package com.shoreline.app.ui.vessel;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.shoreline.app.R;
import com.shoreline.app.data.local.entity.VesselEntity;
import com.shoreline.app.databinding.ItemVesselBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: Custom RecyclerView.Adapter & ViewHolder Pattern
 * Hand-written adapter implementing:
 * 1. ViewBinding per ViewHolder (ItemVesselBinding)
 * 2. Item Click Listener Interface for decoupled navigation
 * 3. Filterable interface for real-time instant search filtering
 * 4. Dynamic badge styling for fitness compliance and overcrowding flags
 */
public class VesselAdapter extends RecyclerView.Adapter<VesselAdapter.VesselViewHolder> implements Filterable {

    public interface OnVesselClickListener {
        void onVesselClick(VesselEntity vessel);
    }

    private final List<VesselEntity> fullList = new ArrayList<>();
    private final List<VesselEntity> displayList = new ArrayList<>();
    private final OnVesselClickListener listener;

    public VesselAdapter(OnVesselClickListener listener) {
        this.listener = listener;
    }

    public void setVessels(List<VesselEntity> vessels) {
        this.fullList.clear();
        this.displayList.clear();
        if (vessels != null) {
            this.fullList.addAll(vessels);
            this.displayList.addAll(vessels);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VesselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVesselBinding binding = ItemVesselBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VesselViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VesselViewHolder holder, int position) {
        VesselEntity vessel = displayList.get(position);
        holder.bind(vessel, listener);
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    // =========================================================================
    // SYLLABUS: Filterable Implementation (Real-time Launch Search)
    // =========================================================================
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<VesselEntity> filtered = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filtered.addAll(fullList);
                } else {
                    String query = constraint.toString().toLowerCase().trim();
                    for (VesselEntity v : fullList) {
                        if (v.getVesselName().toLowerCase().contains(query) ||
                                v.getRegistrationNumber().toLowerCase().contains(query) ||
                                v.getRouteName().toLowerCase().contains(query)) {
                            filtered.add(v);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                displayList.clear();
                if (results.values != null) {
                    displayList.addAll((List<VesselEntity>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    // =========================================================================
    // SYLLABUS: ViewHolder Pattern
    // =========================================================================
    public static class VesselViewHolder extends RecyclerView.ViewHolder {

        private final ItemVesselBinding binding;

        public VesselViewHolder(ItemVesselBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(VesselEntity vessel, OnVesselClickListener listener) {
            Context context = itemView.getContext();

            binding.tvItemVesselName.setText(vessel.getVesselName());
            binding.tvItemRegNumber.setText(String.format("BIWTA Reg: %s", vessel.getRegistrationNumber()));
            binding.tvItemRoute.setText(String.format("Route: %s", vessel.getRouteName()));
            binding.tvItemCapacity.setText(String.format(Locale.US, "Cap: %d", vessel.getLicensedCapacity()));
            binding.tvItemRating.setText(String.format(Locale.US, "★ %.1f", vessel.getCommunityRating()));

            // Fitness Status & Overcrowding Warning Badge
            if (vessel.isFlagged()) {
                binding.tvItemFitnessBadge.setText("FLAGGED (OVERLOAD)");
                binding.tvItemFitnessBadge.setTextColor(ContextCompat.getColor(context, R.color.status_danger_red));
                binding.cardVesselItem.setStrokeColor(ContextCompat.getColor(context, R.color.status_danger_red));
                binding.cardVesselItem.setStrokeWidth(3);
            } else {
                binding.tvItemFitnessBadge.setText("FITNESS VALID");
                binding.tvItemFitnessBadge.setTextColor(ContextCompat.getColor(context, R.color.status_safe_green));
                binding.cardVesselItem.setStrokeColor(ContextCompat.getColor(context, R.color.surface_card_border));
                binding.cardVesselItem.setStrokeWidth(1);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVesselClick(vessel);
                }
            });
        }
    }
}
