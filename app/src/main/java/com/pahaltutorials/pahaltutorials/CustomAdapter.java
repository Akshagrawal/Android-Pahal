package com.pahaltutorials.pahaltutorials;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pahaltutorials.pahaltutorials.util.Util;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private final String[] localDataSet;
    private static SubjectListClickListener mOnClickListener;
    private final boolean showChapter;

    private String fragmentString;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView tvContent, tvNumber;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            itemView.setOnClickListener(this);
            tvContent = (TextView) view.findViewById(R.id.tv_content);
            tvNumber = (TextView) view.findViewById(R.id.tv_number);
        }

        public TextView getTvContent() {
            return tvContent;
        }

        public TextView getTvNumber() {
            return tvNumber;
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(context, localDataSet[getAdapterPosition()], Toast.LENGTH_SHORT).show();
            mOnClickListener.onSubjectListItemClick(getAdapterPosition());
        }
    }

    public interface SubjectListClickListener {
        void onSubjectListItemClick(int clickedItemIndex);
    }

    public CustomAdapter(String[] dataSet, SubjectListClickListener mOnClickListener, String listFragment, boolean showChapter) {
        this.localDataSet = dataSet;
        CustomAdapter.mOnClickListener = mOnClickListener;
        this.fragmentString = listFragment;
        this.showChapter = showChapter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view;
        if (fragmentString.equals("HomeCourseFragment")) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_subjects, viewGroup, false);
        }else{
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_contents, viewGroup, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTvContent().setText(Util.getNameToShow(localDataSet[position]));
        if (showChapter){
            viewHolder.getTvNumber().setVisibility(View.VISIBLE);
            if (position>8)
                viewHolder.getTvNumber().setText(String.format("%d", position + 1));
            else
                viewHolder.getTvNumber().setText(String.format("0%d", position + 1));
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.length;
    }

    public String[] getItemList(){
        return localDataSet;
    }
}

