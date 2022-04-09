package com.pahaltutorials.pahaltutorials;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pahaltutorials.pahaltutorials.model.WebViewModel;
import com.pahaltutorials.pahaltutorials.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddDocument extends AppCompatActivity implements View.OnClickListener {

    final static int PICK_PDF_CODE = 2342;

    private AutoCompleteTextView mClass, mSubject, mContent, mChaptersOrYear, mExercise;
    private Button mSelectPdf, mUploadPdf;
    private TextView mSelectedPdf;

    AlertDialog alertDialog1;

    private String[] classes, subjects, contents, chapters, exercises;

    private List<Uri> multiplePdfUris;

    String selectedSubject;
    String selectedClass;
    String selectedContent;
    String selectedChapter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_document);

        initialiseViews();
        classes = getResources().getStringArray(R.array.select_class);
        subjects = getResources().getStringArray(R.array.subject_list);
        contents = getResources().getStringArray(R.array.contents);

        mClass.setOnClickListener(v -> {
            createAlertDialogWithSingleChoice("Select Class", classes, mClass);
        });

        mSubject.setOnClickListener(v -> {
            createAlertDialogWithSingleChoice("Select Subject", subjects, mSubject);

        });

        mContent.setOnClickListener(v -> {
            createAlertDialogWithSingleChoice("Select Content", contents, mContent);
        });

        mChaptersOrYear.setOnClickListener(v -> {

            selectedSubject = mSubject.getText().toString().toLowerCase();
            selectedClass = mClass.getText().toString().toLowerCase();
            ;
            selectedContent = mContent.getText().toString().toLowerCase();

            if (selectedContent.equals(contents[0].toLowerCase()) ||
                    selectedContent.equals(contents[1].toLowerCase()) ||
                    selectedContent.equals(contents[contents.length - 2].toLowerCase()) ||
                    selectedContent.equals(contents[contents.length - 1].toLowerCase())) {

                if (selectedClass.equals(classes[1]) && selectedSubject.equals(subjects[0].toLowerCase())) {
                    chapters = getResources().getStringArray(R.array.cbse_12th_maths_chapters);
                } else if (selectedClass.equals(classes[1]) && selectedSubject.equals(subjects[1].toLowerCase())) {
                    chapters = getResources().getStringArray(R.array.cbse_12th_ip_chapters);
                } else if (selectedClass.equals(classes[1]) && selectedSubject.equals(subjects[2].toLowerCase())) {
                    chapters = getResources().getStringArray(R.array.cbse_12th_cs_chapters);
                }
            } else if (selectedContent.equals(contents[2].toLowerCase()) &&
                    selectedClass.equals(classes[1]) &&
                    selectedSubject.equals(subjects[0].toLowerCase())) {
                chapters = getResources().getStringArray(R.array.cbse_12th_maths_chapters);
            } else {
                if (selectedClass.equals(classes[1]) && selectedSubject.equals(subjects[0].toLowerCase())) {
                    chapters = getResources().getStringArray(R.array.cbse_12th_maths_previous_years);
                } else if (selectedClass.equals(classes[1]) && selectedSubject.equals(subjects[1].toLowerCase())) {
                    chapters = getResources().getStringArray(R.array.cbse_12th_IP_previous_years);
                } else if (selectedClass.equals(classes[1]) && selectedSubject.equals(subjects[2].toLowerCase())) {
                    chapters = getResources().getStringArray(R.array.cbse_12th_CS_previous_years);
                }
            }
            createAlertDialogWithSingleChoice("Select Chapter or Years", chapters, mChaptersOrYear);
        });
    }

    private void createAlertDialogWithSingleChoice(String heading, String[] values, AutoCompleteTextView textView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(heading);

        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                textView.setText(String.format(values[item].toLowerCase()));
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = builder.create();
        alertDialog1.show();
    }

    private void initialiseViews() {
        mClass = findViewById(R.id.et_class);
        mSubject = findViewById(R.id.et_subject);
        mContent = findViewById(R.id.et_content);
        mChaptersOrYear = findViewById(R.id.et_chapter_year);
        mExercise = findViewById(R.id.et_exercise);
        mSelectPdf = findViewById(R.id.select_pdf);
        mUploadPdf = findViewById(R.id.upload_pdf);
        mSelectedPdf = findViewById(R.id.selected_pdfs);

        mSelectPdf.setOnClickListener(this);
        mUploadPdf.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_pdf:
                getPDF();
                break;
            case R.id.upload_pdf:
                uploadFile();
        }
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
                Log.w("ListFragment", multiplePdfUris.get(i).toString());
                //Log.w("ListFragment", "previousListLink" + previousListLink.get(i));
                //previousListLink.get(i);
            }
        }
    }

    private void uploadFile() {
        //String[] currentChapterList = getResources().getStringArray(R.array.cbse_12th_maths_chapters);

        //selectedChapter = mChaptersOrYear.getText().toString().toLowerCase();
        for (int i = 0; i < multiplePdfUris.size(); i++) {
            if (i < chapters.length - 1) {
                selectedChapter = chapters[i];
            }
            String link = selectedClass + "/" + selectedSubject + "/" + selectedContent + "/" + selectedChapter + "/";
            String filePath = link.substring(0, link.length() - 2);
            String desc = null;
            List<String> words = Arrays.asList(Util.getWordsInString(link));

            StorageReference sRef = FirebaseStorage.getInstance().getReference()
                    .child(filePath);
            sRef.putFile(multiplePdfUris.get(i))
                    .addOnSuccessListener(taskSnapshot -> {
                        //progressBar.setVisibility(View.GONE);

                        sRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            /*WebViewModel webViewModel = new WebViewModel(link, filePath, uri.toString(), desc, words);

                            FirebaseFirestore.getInstance().collection("files")
                                    .add(webViewModel)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "successfully uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    });*/
                        });
                    })
                    .addOnFailureListener(exception -> Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show())

                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //textViewStatus.setText((int) progress + "% Uploading...");
                    });
        }
    }

}