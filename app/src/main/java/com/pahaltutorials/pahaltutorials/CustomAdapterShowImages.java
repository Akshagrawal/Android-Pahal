package com.pahaltutorials.pahaltutorials;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.pahaltutorials.pahaltutorials.model.WebViewModel;
import com.pahaltutorials.pahaltutorials.util.Util;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class CustomAdapterShowImages extends RecyclerView.Adapter<CustomAdapterShowImages.ViewHolder> {

    private final String[] localDataSet;
    private final Context context;
    Bitmap bitmap;
    Uri postImageUri;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            imageView = (ImageView) view.findViewById(R.id.item_contents);
        }

        public ImageView getImageView(){
            return imageView;
        }
    }

    public interface SubjectListClickListener {
        void onSubjectListItemClick(int clickedItemIndex);
    }

    public CustomAdapterShowImages(Context context, String[] dataSet) {
        this.localDataSet = dataSet;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view;
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.show_images, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        try {
            Glide.with(context)
                    .asBitmap()
                    .load(localDataSet[position])
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    bitmap = resource;
                    //postImageUri = getImageUri();
                    viewHolder.getImageView().setImageBitmap(Bitmap.createBitmap(bitmap, 100, 150, bitmap.getWidth()-200, bitmap.getHeight()-250, null, false));
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        }catch (Exception e){

        }
    }

    private Uri getImageUri() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, UUID.randomUUID().toString() + ".png", "drawing");
        return Uri.parse(path);
    }

    @Override
    public int getItemCount() {
        return localDataSet.length;
    }
}

