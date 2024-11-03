package com.gov.sindhpolice.beatbook.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gov.sindhpolice.beatbook.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<Uri> imageUris;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onDeleteClick(Uri uri);
    }

    public ImageAdapter(List<Uri> imageUris, OnImageClickListener listener) {
        this.imageUris = imageUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        String imageName = getImageName(imageUri); // Extract the image name from the URI
        holder.bind(imageUri, imageName, listener); // Pass the imageUri as well
    }

    // Method to extract the image name from the URI
    private String getImageName(Uri uri) {
        String[] segments = uri.getPath().split("/");
        return segments[segments.length - 1]; // Return the last segment as the image name
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_image;
        private Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_image = itemView.findViewById(R.id.tv_image);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Uri imageUri, String imageName, OnImageClickListener listener) {
            tv_image.setText(imageName); // Set the extracted image name
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(imageUri)); // Pass the correct Uri
        }
    }
}
