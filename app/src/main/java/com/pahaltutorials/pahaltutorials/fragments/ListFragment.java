package com.pahaltutorials.pahaltutorials.fragments;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pahaltutorials.pahaltutorials.CustomAdapter;
import com.pahaltutorials.pahaltutorials.R;
import com.pahaltutorials.pahaltutorials.model.DataModel;
import com.pahaltutorials.pahaltutorials.model.WebViewModel;
import com.pahaltutorials.pahaltutorials.preferences.ClassPreferences;
import com.pahaltutorials.pahaltutorials.preferences.PagePreferences;
import com.pahaltutorials.pahaltutorials.util.Util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;

@SuppressWarnings("ALL")
public class ListFragment extends Fragment implements CustomAdapter.SubjectListClickListener, OnPageChangeListener, OnLoadCompleteListener {

    private static final String KEY_CURRENT_LOCATION = "currentLocation";
    private static final String KEY_PREVIOUS_SCREEN_LIST = "previousList";
    private static final String KEY_PREVIOUS_SCREEN_CONTENT = "previousListContent";
    private static final String FIRESTORE_FIELD_LINKS = "links";
    private static final String FIRESTORE_COLLECTION_CONTENTS = "contents";
    final static int PICK_PDF_CODE = 2342;
    final static int PICK_IMAGE_CODE = 2341;
    private static final String PDF_FILE_EXTENSION = ".pdf";
    private static final String KEY_CURRENT_SCREEN_LIST = "currentList";
    private static final float MIN_ZOOM = 1.3f;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    private ConstraintLayout mLayoutPdfView;
    private static final int SPAN_COUNT = 2;
    protected List<String> mDataSet;
    protected List<String> mDataLinks;
    protected List<String> previousLinkList;
    protected List<String> previousContentList;
    private String currentLocation;

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected LayoutManagerType mCurrentLayoutManagerType;
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private ImageView mOnBackPressed;
    private TextView webViewDetail;
    private EditText mNumberOfPages, mStartPage;
    private Button mUploadFile;
    private ProgressBar progressBar;
    private PDFView pdfView;

    //private RecyclerView mRecyclerViewShowImages;
    //private CustomAdapterShowImages mAdapterShowImages;

    private SwitchCompat switchCompat;

    private List<Uri> multiplePdfUris;
    private List<Uri> multipleImageUris;
    private PdfDownload task;
    private String pdfDocumentId;

    @Override
    public void onPageChanged(int page, int pageCount) {
        if(pdfDocumentId != null)
            PagePreferences.setCurrentPage(getContext(), pdfDocumentId, page);
    }

    @Override
    public void loadComplete(int nbPages) {
        int page = PagePreferences.getCurrentPage(getContext(), pdfDocumentId);
        Log.w("scrolling", "savedPage" + page);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_list, container, false);

        initialiseViews(view);

        setCurrentLocation();
        setPreviousListLink();
        setPreviousContentList();

        setupRecycler();

        mOnBackPressed.setOnClickListener(v -> getActivity().onBackPressed());

        if (savedInstanceState != null) {
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) uploadPdfFile(view) /*uploadImages(view)*/;
                else loadPdf() /*loadImages()*/;
            }
        });

        FirebaseFirestore.getInstance().collection(FIRESTORE_COLLECTION_CONTENTS)
                .whereArrayContains(FIRESTORE_FIELD_LINKS, Util.getLink(getContext(), currentLocation))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                DataModel dataModel;
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    dataModel = snapshot.toObject(DataModel.class);
                    mDataSet = dataModel.getContent();
                    mDataLinks = dataModel.getLinks();
                }
                if (mDataSet != null) {
                    setAdapter(mDataSet);
                } else {
                    loadPdf();
                    //loadImages();
                }
            }
        });
        return view;
    }

    private void loadImages() {
        mRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        mLayoutPdfView.setVisibility(View.VISIBLE);

        mNumberOfPages.setVisibility(View.GONE);
        mStartPage.setVisibility(View.GONE);
        mUploadFile.setVisibility(View.GONE);

        FirebaseFirestore.getInstance().collection("files")
                .whereEqualTo("docLink", Util.getLink(getContext(), currentLocation))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<String> imageDownloadLinks = new ArrayList<String>();
                        for (DocumentSnapshot snapshots : queryDocumentSnapshots) {
                            //imageDownloadLinks = snapshots.toObject(WebViewModel.class).getDownloadLinks();
                        }
                        Collections.sort(imageDownloadLinks);
                        Log.w("ListFragment", imageDownloadLinks.toString());
                        mLayoutManager = new LinearLayoutManager(getActivity());
                        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                        //mRecyclerViewShowImages.setLayoutManager(mLayoutManager);
                        //mAdapterShowImages = new CustomAdapterShowImages(getContext(), imageDownloadLinks.toArray(new String[imageDownloadLinks.size()]));
                        //mRecyclerViewShowImages.setAdapter(mAdapterShowImages);
                    }
                });
    }

    private void uploadImages(View view) {
        mNumberOfPages.setVisibility(View.VISIBLE);
        mStartPage.setVisibility(View.VISIBLE);
        mUploadFile.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        //mRecyclerViewShowImages.setVisibility(View.GONE);

        //mLink.setText(currentLocation);
        String link = ClassPreferences.getCurrentClass(getContext()) + "/" + mNumberOfPages.getText().toString().toLowerCase();
        String fileName = link.substring(0, link.length() - 1) + ".pdf";

        //mFileName.setText(Util.getWordsInString(link)[Util.getWordsInString(link).length - 1]);
        mUploadFile.setOnClickListener(v -> {
            //getPDF();
            getImages();
        });
    }

    private void getImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_CODE);
    }

    private void uploadPdfFile(View view) {
        mNumberOfPages.setVisibility(View.VISIBLE);
        mStartPage.setVisibility(View.VISIBLE);
        mUploadFile.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        pdfView.setVisibility(View.GONE);

        //mLink.setText(currentLocation);

        String link = ClassPreferences.getCurrentClass(getContext()) + "/" + mNumberOfPages.getText().toString().toLowerCase();
        String fileName = link.substring(0, link.length() - 1) + ".pdf";

        //mFileName.setText(Util.getWordsInString(link)[Util.getWordsInString(link).length - 1]);
        mUploadFile.setOnClickListener(v -> {
            getPDF();
        });
    }

    private void loadPdf() {
        mRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        mLayoutPdfView.setVisibility(View.VISIBLE);

        mNumberOfPages.setVisibility(View.GONE);
        mStartPage.setVisibility(View.GONE);
        mUploadFile.setVisibility(View.GONE);

        String link = Util.getLink(getContext(), currentLocation).toLowerCase();
        FirebaseFirestore.getInstance().collection("files")
                .whereEqualTo("storagePath", link)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    WebViewModel webViewModel = snapshot.toObject(WebViewModel.class);
                    if (webViewModel.getStoragePath().equals(link)) {
                        pdfDocumentId = snapshot.getId();
                        task = new PdfDownload();
                        task.execute(webViewModel.getDownloadLink());
                    }
                }
            }
        });

        /*String filePathName = link.substring(0, link.length() - 1) + PDF_FILE_EXTENSION;
        FirebaseStorage.getInstance().getReference()
                .child(filePathName).getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    try {
                        task = new pdfDownload().execute(uri.toString());
                    } catch (Exception e) {

                    }
                });*/
    }

    @SuppressLint("StaticFieldLeak")
    public class PdfDownload extends AsyncTask<String, Void, InputStream> {

        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null;

            try {
                URL url = new URL(strings[0]);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            progressBar.setVisibility(View.GONE);
            pdfView.setVisibility(View.VISIBLE);
            //pdfView.setMaxZoom(MIN_ZOOM);

            int page = PagePreferences.getCurrentPage(getContext(), pdfDocumentId);
            Log.w("scrolling", "savedPage: " + page);
            //pdfView.jumpTo(page);
            try {
                pdfView.fromStream(inputStream)
                        .pageFitPolicy(FitPolicy.BOTH)
                        .fitEachPage(true)
                        .defaultPage(page)
                        .onPageChange(ListFragment.this::onPageChanged)
                        .onLoad(ListFragment.this::loadComplete)
                        .enableDoubletap(false)
                        .load();
            } catch (Exception e){
                e.printStackTrace();
            }
            //pdfView.zoomTo(MIN_ZOOM);
            //pdfView.setScrollX((int)Math.ceil(pdfView.getWidth() * 0.1481));
        }
    }

    private void setAdapter(List<String> mDataSet) {
        String lastLinkContent = Util.getlastLinkContent(currentLocation);

        boolean showChapter = false;
        if (Arrays.asList(getResources().getStringArray(R.array.chapter_visible)).contains(lastLinkContent))
            showChapter = true;
        mAdapter = new CustomAdapter(mDataSet.toArray(new String[0]), this, "", showChapter);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupRecycler() {
        mLayoutManager = new LinearLayoutManager(getActivity());
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void setPreviousListLink() {
        if (getArguments().getStringArrayList(KEY_PREVIOUS_SCREEN_LIST) == null) return;
        previousLinkList = getArguments().getStringArrayList(KEY_PREVIOUS_SCREEN_LIST);
    }

    private void setPreviousContentList() {
        if (getArguments().getStringArrayList(KEY_PREVIOUS_SCREEN_CONTENT) == null) return;
        previousContentList = getArguments().getStringArrayList(KEY_PREVIOUS_SCREEN_CONTENT);
    }

    private void setCurrentLocation() {
        currentLocation = getArguments().getString(KEY_CURRENT_LOCATION).toLowerCase();
    }

    private void initialiseViews(View view) {
        webViewDetail = view.findViewById(R.id.web_view_detail);
        progressBar = view.findViewById(R.id.tab_web_progress_bar);
        mLayoutPdfView = view.findViewById(R.id.layout_pdf_view);
        mRecyclerView = view.findViewById(R.id.recycler_content_list);
        mOnBackPressed = view.findViewById(R.id.onBackPressed);
        pdfView = view.findViewById(R.id.pdf_view);
        //mRecyclerViewShowImages = view.findViewById(R.id.recycler_show_images);
        switchCompat = view.findViewById(R.id.switch_toggle);
        mNumberOfPages = view.findViewById(R.id.et_number_of_pages);
        mStartPage = view.findViewById(R.id.et_start_page);
        mUploadFile = view.findViewById(R.id.buttonUploadFile);
        progressBar = view.findViewById(R.id.tab_web_progress_bar);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onSubjectListItemClick(int clickedItemIndex) {
        Toast.makeText(getContext(), mAdapter.getItemList()[clickedItemIndex], Toast.LENGTH_SHORT).show();
        setFragment(new ListFragment(), currentLocation + mAdapter.getItemList()[clickedItemIndex] + "/");
    }

    private void setFragment(Fragment fragment, String newLocation) {
        Bundle args = new Bundle();
        args.putString(KEY_CURRENT_LOCATION, newLocation);
        args.putStringArrayList(KEY_PREVIOUS_SCREEN_LIST, (ArrayList<String>) mDataLinks);
        args.putStringArrayList(KEY_PREVIOUS_SCREEN_CONTENT, (ArrayList<String>) mDataSet);
        fragment.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment).addToBackStack(null).commit();
    }

    private void getPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Document"), PICK_PDF_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null) {

            multiplePdfUris = new ArrayList<Uri>();
            if (null != data.getClipData()) { // checking multiple selection or not
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();

                    multiplePdfUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                multiplePdfUris.add(data.getData());
                //uploadFile(data.getData());
            }
            for (int i = 0; i < multiplePdfUris.size(); i++) {
                //Log.w("ListFragment", multiplePdfUris.get(i).toString());
                //Log.w("ListFragment", "previousListLink" + previousLinkList.get(i));
                //previousLinkList.get(i);
            }

            uploadFile(multiplePdfUris);
        } else if (requestCode == PICK_IMAGE_CODE && resultCode == RESULT_OK && data != null) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            List<String> imagesEncodedList = new ArrayList<>();
            String imageEncoded;
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                multipleImageUris = new ArrayList<Uri>();
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    multipleImageUris.add(data.getClipData().getItemAt(i).getUri());
                }

                for (int i = 0; i < multipleImageUris.size(); i++) {
                    //Log.w("ListFragment", multipleImageUris.get(i).toString());
                    //Log.w("ListFragment", "previousListLink" + previousListLink.get(i));
                    //previousListLink.get(i);
                }
            }
            uploadImageFile(multipleImageUris);
        }
    }

    private String getFileName(Uri uri) {
        String result;

        //if uri is content
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    //local filesystem
                    int index = cursor.getColumnIndex("_data");
                    if (index == -1)
                        //google drive
                        index = cursor.getColumnIndex("_display_name");
                    result = cursor.getString(index);
                    if (result != null)
                        uri = Uri.parse(result);
                    else
                        return null;
                }
            } finally {
                cursor.close();
            }
        }

        result = uri.getPath();

        //get filename + ext of path
        int cut = result.lastIndexOf('/');
        if (cut != -1)
            result = result.substring(cut + 1);
        return result;
    }

    private void uploadImageFile(List<Uri> multipleImageUris) {
        String[] currentChapterList = getResources().getStringArray(R.array.cbse_12th_maths_chapters);
        progressBar.setVisibility(View.VISIBLE);

        int numberOfPages = Integer.parseInt(mNumberOfPages.getText().toString());
        int startPage = Integer.parseInt(mStartPage.getText().toString());
        List<String> downloadLinks = new ArrayList<>();
        for (int i = 0; i < multipleImageUris.size(); i++) {

            String link = Util.getLink(getContext(), currentLocation) + "/page" + Util.getPageNumber(startPage + i);
            String filePath = link + ".png";
            String desc = null;
            //List<String> words = Arrays.asList(Util.getWordsInString(link));

            StorageReference sRef = FirebaseStorage.getInstance().getReference()
                    .child(filePath);
            sRef.putFile(multipleImageUris.get(i))
                    .addOnSuccessListener(taskSnapshot -> {
                        progressBar.setVisibility(View.GONE);

                        sRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            downloadLinks.add(uri.toString());

                            if (downloadLinks.size() == multipleImageUris.size()) {
                                //WebViewModel webViewModel = new WebViewModel(link, filePath, uri.toString(), desc, words);

                                Collections.sort(downloadLinks);
                                //WebViewModel webViewModel = new WebViewModel(Util.getLink(getContext(), currentLocation), desc, downloadLinks, startPage, numberOfPages);
                                /*FirebaseFirestore.getInstance().collection("files")
                                        .add(webViewModel)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "successfully uploaded", Toast.LENGTH_SHORT).show();
                                            }
                                        });*/
                            }
                        });
                    });
        }
    }

    private void uploadFile(List<Uri> multiplePdfUris) {
        String[] currentChapterList = previousContentList.toArray(new String[0]);
        progressBar.setVisibility(View.VISIBLE);
        for (int i = 0; i < multiplePdfUris.size(); i++) {
            String link = Util.getLinkToAdd(getContext(), currentLocation) + previousContentList.get(i).toLowerCase() + "/";
            String fileName = getFileName(multiplePdfUris.get(i)).toLowerCase().replace("-min", "")/*.substring(4)*/;
            String filePath = link + fileName;
            String desc = null;
            Log.w("ListFragment", "storagePath: " + link);
            Log.w("ListFragment", "fileName: " + fileName);
            //List<String> words = Arrays.asList(Util.getWordsInString(link));

            StorageReference sRef = FirebaseStorage.getInstance().getReference()
                    .child(filePath);
            sRef.putFile(multiplePdfUris.get(i))
                    .addOnSuccessListener(taskSnapshot -> {
                        progressBar.setVisibility(View.GONE);

                        sRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            WebViewModel webViewModel = new WebViewModel(null, 0, 0, link, fileName, uri.toString());
                            FirebaseFirestore.getInstance().collection("files")
                                    .add(webViewModel)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), fileName, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            //WebViewModel webViewModel = new WebViewModel(link, filePath, uri.toString(), desc, words);

                            /*Map<String, String> downloadLink = new HashMap<>();
                            FirebaseFirestore.getInstance().collection("files")
                                    .whereEqualTo("storagePath", link)
                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                        WebViewModel webViewModel = snapshot.toObject(WebViewModel.class);
                                        if (webViewModel.getStoragePath().equals(link)) {
                                            downloadLink.put("downloadLink", uri.toString());
                                            downloadLink.put("fileName", fileName);
                                            FirebaseFirestore.getInstance().collection("files")
                                                    .document(snapshot.getId())
                                                    .set(downloadLink, SetOptions.merge())
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getContext(), "successfully uploaded", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });*/
                        });
                    })
                    .addOnFailureListener(exception -> Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show())
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //textViewStatus.setText((int) progress + "% Uploading...");
                    });
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        //check the state of the task
        if (task != null && task.getStatus() == AsyncTask.Status.RUNNING)
            task.cancel(true);

        //check the state of the task
        if (task != null && task.getStatus() == AsyncTask.Status.FINISHED)
            task.cancel(true);
    }
}
