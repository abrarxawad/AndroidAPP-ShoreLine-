package com.shoreline.app.ui.route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.shoreline.app.R;
import com.shoreline.app.data.model.GhatNode;
import com.shoreline.app.data.model.NodeWeather;
import com.shoreline.app.databinding.ItemRouteNodeWeatherBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SYLLABUS: Multi-Item RecyclerView Adapter - Route Checkpoint Weather
 * Renders hourly weather observations for each intermediate waypoint on the chosen waterway route.
 */
public class RouteNodeWeatherAdapter extends RecyclerView.Adapter<RouteNodeWeatherAdapter.NodeWeatherViewHolder> {

    private final List<GhatNode> intermediateNodes = new ArrayList<>();
    private Map<String, NodeWeather> weatherMap;
    private String worstNodeId = "";
    private int vesselSpeedKmh = 25;

    public void setData(List<GhatNode> fullPath, Map<String, NodeWeather> weatherMap,
                        String worstNodeId, int vesselSpeedKmh) {
        this.intermediateNodes.clear();
        // Intermediate nodes: skip origin (index 0) and destination (last index) if route has > 2 nodes
        if (fullPath != null && fullPath.size() > 2) {
            for (int i = 1; i < fullPath.size() - 1; i++) {
                this.intermediateNodes.add(fullPath.get(i));
            }
        } else if (fullPath != null) {
            // In 2-node routes, display all points
            this.intermediateNodes.addAll(fullPath);
        }
        this.weatherMap = weatherMap;
        this.worstNodeId = worstNodeId;
        this.vesselSpeedKmh = Math.max(10, vesselSpeedKmh);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NodeWeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRouteNodeWeatherBinding binding = ItemRouteNodeWeatherBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NodeWeatherViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NodeWeatherViewHolder holder, int position) {
        GhatNode node = intermediateNodes.get(position);
        NodeWeather weather = (weatherMap != null) ? weatherMap.get(node.getId()) : null;
        boolean isWorst = node.getId().equals(worstNodeId);
        holder.bind(node, weather, position + 1, isWorst);
    }

    @Override
    public int getItemCount() {
        return intermediateNodes.size();
    }

    static class NodeWeatherViewHolder extends RecyclerView.ViewHolder {
        private final ItemRouteNodeWeatherBinding binding;

        NodeWeatherViewHolder(ItemRouteNodeWeatherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GhatNode node, NodeWeather weather, int stepNumber, boolean isWorst) {
            binding.tvNodeStepIndex.setText(String.valueOf(stepNumber));
            binding.tvNodeGhatName.setText(node.getName());
            binding.tvNodeRiverDetails.setText(String.format(Locale.US, "%s River • Division: %s",
                    node.getRiver(), node.getDivision()));

            if (isWorst && weather != null && weather.getRiskLevel() > NodeWeather.RISK_LOW) {
                binding.tvWorstPointTag.setVisibility(View.VISIBLE);
            } else {
                binding.tvWorstPointTag.setVisibility(View.GONE);
            }

            if (weather != null) {
                binding.tvNodeWeatherMetrics.setText(weather.getMetricsSummary());
                binding.tvNodeCondition.setText(weather.getWeatherDescription());

                int riskLevel = weather.getRiskLevel();
                if (riskLevel == NodeWeather.RISK_HIGH) {
                    binding.tvNodeRiskBadge.setText("⛔ DANGER");
                    binding.tvNodeRiskBadge.setBackgroundResource(R.drawable.bg_chip_flagged);
                    binding.tvNodeRiskBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_danger_text));
                } else if (riskLevel == NodeWeather.RISK_MEDIUM) {
                    binding.tvNodeRiskBadge.setText("⚠ CAUTION");
                    binding.tvNodeRiskBadge.setBackgroundResource(R.drawable.bg_chip_flagged);
                    binding.tvNodeRiskBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.secondary_variant_amber));
                } else {
                    binding.tvNodeRiskBadge.setText("✓ SAFE");
                    binding.tvNodeRiskBadge.setBackgroundResource(R.drawable.bg_chip_safe);
                    binding.tvNodeRiskBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_safe_text));
                }
            } else {
                binding.tvNodeWeatherMetrics.setText("Weather data synchronizing…");
                binding.tvNodeCondition.setText("Awaiting Open-Meteo river forecast");
                binding.tvNodeRiskBadge.setText("PENDING");
            }
        }
    }
}
