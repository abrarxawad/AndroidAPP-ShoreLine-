package com.shoreline.app.ui.guide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shoreline.app.R;
import com.shoreline.app.databinding.ItemRescueGuideBinding;
import com.shoreline.app.model.RescueGuide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter for rendering expandable water rescue and survival guide cards.
 */
public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> {

    private final List<RescueGuide> guides = new ArrayList<>();
    private final Set<Long> expandedIds = new HashSet<>();

    public void setGuides(List<RescueGuide> newGuides) {
        guides.clear();
        if (newGuides != null) {
            guides.addAll(newGuides);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRescueGuideBinding binding = ItemRescueGuideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GuideViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        holder.bind(guides.get(position));
    }

    @Override
    public int getItemCount() {
        return guides.size();
    }

    class GuideViewHolder extends RecyclerView.ViewHolder {
        private final ItemRescueGuideBinding binding;

        GuideViewHolder(ItemRescueGuideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RescueGuide guide) {
            binding.tvGuideCategory.setText(guide.getCategory());
            binding.tvGuideTitleEn.setText(guide.getTitleEn());
            binding.tvGuideTitleBn.setText(guide.getTitleBn());
            binding.tvGuideSummary.setText(guide.getSummaryEn());

            // Set appropriate icon
            String icon = guide.getIconName();
            if ("ic_lifejacket".equals(icon)) {
                binding.ivGuideIcon.setImageResource(R.drawable.ic_lifejacket);
            } else if ("ic_firstaid".equals(icon)) {
                binding.ivGuideIcon.setImageResource(R.drawable.ic_firstaid);
            } else if ("ic_whistle".equals(icon)) {
                binding.ivGuideIcon.setImageResource(R.drawable.ic_whistle);
            } else if ("ic_vessel".equals(icon)) {
                binding.ivGuideIcon.setImageResource(R.drawable.ic_vessel);
            } else {
                binding.ivGuideIcon.setImageResource(R.drawable.ic_shoreline_logo);
            }

            // Populate formatted steps
            StringBuilder sb = new StringBuilder();
            List<String> steps = guide.getStepsList();
            for (int i = 0; i < steps.size(); i++) {
                sb.append(steps.get(i));
                if (i < steps.size() - 1) {
                    sb.append("\n\n");
                }
            }
            binding.tvStepsBody.setText(sb.toString());

            // Handle expansion state
            boolean isExpanded = expandedIds.contains(guide.getId());
            binding.layoutExpandedSteps.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            binding.tvExpandIndicator.setText(isExpanded ? R.string.guide_collapse : R.string.guide_expand);

            binding.cardGuide.setOnClickListener(v -> {
                if (expandedIds.contains(guide.getId())) {
                    expandedIds.remove(guide.getId());
                } else {
                    expandedIds.add(guide.getId());
                }
                notifyItemChanged(getAdapterPosition());
            });
        }
    }
}

