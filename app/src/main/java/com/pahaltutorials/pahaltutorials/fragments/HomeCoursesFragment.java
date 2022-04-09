package com.pahaltutorials.pahaltutorials.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pahaltutorials.pahaltutorials.AddDocument;
import com.pahaltutorials.pahaltutorials.CustomAdapter;
import com.pahaltutorials.pahaltutorials.R;
import com.pahaltutorials.pahaltutorials.model.DataModel;
import com.pahaltutorials.pahaltutorials.preferences.ClassPreferences;

import java.util.ArrayList;
import java.util.List;

public class HomeCoursesFragment extends Fragment implements CustomAdapter.SubjectListClickListener {

    private static final String KEY_CURRENT_LOCATION = "currentLocation";
    private static final String KEY_PREVIOUS_SCREEN_LIST = "previousList";

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    private static final int SPAN_COUNT = 2;
    AlertDialog alertDialog1;
    protected String[] mDataset;
    protected List<String> mDataLinks;

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected LayoutManagerType mCurrentLayoutManagerType;
    private TextView mCurrentClass;
    private ImageView mEditClass;
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";

    private TextView helloStudent;
    String currentClass = null;

    @Override
    public void onSubjectListItemClick(int clickedItemIndex) {
        Toast.makeText(getContext(), mAdapter.getItemList()[clickedItemIndex], Toast.LENGTH_SHORT).show();

        if (currentClass == null) createAlertDialogWithSingleChoice();
        else setFragment(new ListFragment(), mAdapter.getItemList()[clickedItemIndex]);
        //SM.sendData(mAdapter.getItemList()[clickedItemIndex]);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_courses, container, false);
        //mDataset = getResources().getStringArray(R.array.subject_list);
        mRecyclerView = view.findViewById(R.id.recycler_subject_list);
        mCurrentClass = view.findViewById(R.id.student_class);
        mEditClass = view.findViewById(R.id.edit_class);
        currentClass = ClassPreferences.getCurrentClass(getContext());
        helloStudent = view.findViewById(R.id.hello_student);

        helloStudent.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddDocument.class);
            startActivity(intent);
        });

        if (currentClass != null) {
            mCurrentClass.setText(String.format("Class %s", currentClass));
        } else {
            createAlertDialogWithSingleChoice();
        }
        mLayoutManager = new LinearLayoutManager(getActivity());
        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        mRecyclerView.setLayoutManager(mLayoutManager);
        //setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        mEditClass.setOnClickListener(v -> createAlertDialogWithSingleChoice());

        FirebaseFirestore.getInstance().collection("subjects").whereArrayContains("links", "12th/")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataModel dataModel;
                for (DocumentSnapshot snapshot : task.getResult().getDocuments()) {
                    dataModel = snapshot.toObject(DataModel.class);
                    mDataset = dataModel.getContent().toArray(new String[0]);
                    mDataLinks = dataModel.getLinks();
                }
                if (mDataset != null) {
                    mAdapter = new CustomAdapter(mDataset, this, "HomeCourseFragment", false);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        });

        return view;
    }

    private void createAlertDialogWithSingleChoice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Class");

        String[] values = getResources().getStringArray(R.array.select_class);
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                /*switch (item) {
                    case 0:
                        Toast.makeText(getContext(), "First Item Clicked", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Toast.makeText(getContext(), "Second Item Clicked", Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(getContext(), "Third Item Clicked", Toast.LENGTH_LONG).show();
                        break;
                }*/
                mCurrentClass.setText(String.format("Class %s", values[item]));
                ClassPreferences.setCurrentClass(getContext(), values[item]);
                currentClass = ClassPreferences.getCurrentClass(getContext());
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = builder.create();
        alertDialog1.show();
    }

    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:

            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setFragment(Fragment fragment, String value) {
        Bundle args = new Bundle();
        args.putString(KEY_CURRENT_LOCATION, value + "/");
        args.putStringArrayList(KEY_PREVIOUS_SCREEN_LIST, (ArrayList<String>) mDataLinks);
        fragment.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment).addToBackStack(null).commit();
    }
}