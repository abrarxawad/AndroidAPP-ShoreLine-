package com.shoreline.app.ui.route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shoreline.app.R;
import com.shoreline.app.data.model.GhatNode;
import com.shoreline.app.data.model.WaterwayEdge;
import com.shoreline.app.data.model.WeatherInfo;
import com.shoreline.app.graph.RiverGraphRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: Multi-View-Type RecyclerView Adapter (RecyclerView 3)
 * Displays river voyage steps alongside in-line meteorological safety advisories.
 */
public class RouteStepAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_STEP = 1;
    public static final int VIEW_TYPE_WEATHER_ALERT = 2;

    public static class DisplayItem {
        final int viewType;
        final GhatNode ghat;
        final WaterwayEdge incomingEdge;
        final int stepIndex;
        final boolean isLast;
        final WeatherInfo weather;
        final String warningTitle;
        final String warningBody;

        // Constructor for Step
        DisplayItem(GhatNode ghat, WaterwayEdge edge, int stepIndex, boolean isLast) {
            this.viewType = VIEW_TYPE_STEP;
            this.ghat = ghat;
            this.incomingEdge = edge;
            this.stepIndex = stepIndex;
            this.isLast = isLast;
            this.weather = null;
            this.warningTitle = null;
            this.warningBody = null;
        }

        // Constructor for Weather Alert
        DisplayItem(WeatherInfo weather, String title, String body) {
            this.viewType = VIEW_TYPE_WEATHER_ALERT;
            this.ghat = null;
            this.incomingEdge = null;
            this.stepIndex = -1;
            this.isLast = false;
            this.weather = weather;
            this.warningTitle = title;
            this.warningBody = body;
        }
    }

    private final List<DisplayItem> items = new ArrayList<>();

    public void setRouteResult(RiverGraphRouter.RouteResult result, WeatherInfo weather) {
        items.clear();
        if (result == null || result.path.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        // If weather warrants caution, insert an in-line alert banner at the top of the route
        if (weather != null && weather.getRiverSignalNumber() >= 2) {
            String title = String.format(Locale.US, "BMD River Warning (Signal %d Active)", weather.getRiverSignalNumber());
            String body = String.format(Locale.US, "Wind: %.1f km/h (Gusts: %.1f km/h). %s",
                    weather.getWindSpeedKmh(), weather.getWindGustKmh(), result.weatherNotice);
            items.add(new DisplayItem(weather, title, body));
        }

        // Add waypoint steps
        for (int i = 0; i < result.path.size(); i++) {
            GhatNode node = result.path.get(i);
            WaterwayEdge edge = (i > 0 && i - 1 < result.edges.size()) ? result.edges.get(i - 1) : null;
            boolean isLast = (i == result.path.size() - 1);
            items.add(new DisplayItem(node, edge, i + 1, isLast));
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_WEATHER_ALERT) {
            View view = inflater.inflate(R.layout.item_route_weather_warning, parent, false);
            return new WeatherAlertViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_route_step, parent, false);
            return new StepViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = items.get(position);

        if (holder instanceof StepViewHolder) {
            StepViewHolder stepHolder = (StepViewHolder) holder;
            stepHolder.tvStepIndex.setText(String.valueOf(item.stepIndex));
            stepHolder.tvGhatName.setText(item.ghat.getName());
            stepHolder.tvRiverDetails.setText(String.format(Locale.US, "River: %s | Division: %s",
                    item.ghat.getRiver(), item.ghat.getDivision()));

            if (item.incomingEdge != null) {
                stepHolder.tvSegmentDetails.setText(String.format(Locale.US,
                        "Segment: %.1f km (~%d mins) | Channel: %s",
                        item.incomingEdge.getDistanceKm(),
                        item.incomingEdge.getBaseTimeMinutes(),
                        item.incomingEdge.getChannelType()));
                stepHolder.tvSegmentDetails.setVisibility(View.VISIBLE);
            } else {
                stepHolder.tvSegmentDetails.setText("Origin Ghat Departure");
            }

            stepHolder.lineConnector.setVisibility(item.isLast ? View.GONE : View.VISIBLE);

        } else if (holder instanceof WeatherAlertViewHolder) {
            WeatherAlertViewHolder alertHolder = (WeatherAlertViewHolder) holder;
            alertHolder.tvTitle.setText(item.warningTitle);
            alertHolder.tvBody.setText(item.warningBody);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        final TextView tvStepIndex;
        final TextView tvGhatName;
        final TextView tvRiverDetails;
        final TextView tvSegmentDetails;
        final View lineConnector;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepIndex = itemView.findViewById(R.id.tvStepIndex);
            tvGhatName = itemView.findViewById(R.id.tvStepGhatName);
            tvRiverDetails = itemView.findViewById(R.id.tvStepRiverDetails);
            tvSegmentDetails = itemView.findViewById(R.id.tvStepSegmentDetails);
            lineConnector = itemView.findViewById(R.id.lineStepConnector);
        }
    }

    static class WeatherAlertViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvBody;

        WeatherAlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvWarningTitle);
            tvBody = itemView.findViewById(R.id.tvWarningBody);
        }
    }
}
