package com.gov.sindhpolice.beatbook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.models.ListAll;
import com.gov.sindhpolice.beatbook.ui.dashboard.DashboardFragment;

import java.util.ArrayList;

public class BookEntryAdapter extends RecyclerView.Adapter<BookEntryAdapter.ListViewHolder> {

    private ArrayList<ListAll> entryList;
    private DashboardFragment fragment; // Reference to the fragment

    public BookEntryAdapter(ArrayList<ListAll> entryList, DashboardFragment fragment) {
        this.entryList = entryList;
        this.fragment = fragment; // Initialize the fragment reference
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_item, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        ListAll entry = entryList.get(position);
        holder.tvTitle.setText(entry.getTitle());
        holder.tvCatName.setText(entry.getCategory());
        holder.tvCreatedBy.setText(entry.getCreatedBy());
        holder.tvCreatedAt.setText(entry.getCreatedAt());

        // Set the click listener for the item
        holder.itemView.setOnClickListener(v -> {
            int entryId = entry.getId(); // Get the ID of the clicked entry
            fragment.onEntryClick(entryId); // Call the click method in the fragment
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCatName, tvCreatedBy, tvCreatedAt;

        ListViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvCatName = itemView.findViewById(R.id.tv_catName);
            tvCreatedBy = itemView.findViewById(R.id.tv_createtBy);
            tvCreatedAt = itemView.findViewById(R.id.tv_createdAt);
        }
    }
}
