package com.gov.sindhpolice.beatbook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gov.sindhpolice.beatbook.R;
import com.gov.sindhpolice.beatbook.models.DetailItem;

import java.util.List;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.DetailViewHolder> {

    private List<DetailItem> detailItems;

    // Constructor
    public DetailAdapter(List<DetailItem> detailItems) {
        this.detailItems = detailItems;
    }

    // ViewHolder class
    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvLat, tvLong, tvAddress, tvContactNo, tvCreatedBy, tvCreatedAt, tvUpdatedAt, tvDescription;

        public DetailViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvLat = itemView.findViewById(R.id.tv_lat);
            tvLong = itemView.findViewById(R.id.tv_long);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvContactNo = itemView.findViewById(R.id.tv_contact_no);
            tvCreatedBy = itemView.findViewById(R.id.tv_created_by);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            tvUpdatedAt = itemView.findViewById(R.id.tv_updated_at);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_item, parent, false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        DetailItem item = detailItems.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvCategory.setText(item.getCategory());
        holder.tvLat.setText(item.getLatitude());
        holder.tvLong.setText(item.getLongitude());
        holder.tvAddress.setText(item.getAddress());
        holder.tvContactNo.setText(item.getContactNo());
        holder.tvCreatedBy.setText(item.getCreatedBy());
        holder.tvCreatedAt.setText(item.getCreatedAt());
        holder.tvUpdatedAt.setText(item.getUpdatedAt());
        holder.tvDescription.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return detailItems.size();
    }
}
