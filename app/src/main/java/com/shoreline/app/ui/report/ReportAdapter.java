package com.shoreline.app.ui.report;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.shoreline.app.R;
import com.shoreline.app.data.local.entity.OverloadReportEntity;
import com.shoreline.app.util.ImageCompressionHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: RecyclerView Adapter 2 - Overload Reports Queue
 * Displays cards with sync status badges (Room Offline Queue vs Firestore Uploaded).
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    public interface OnReportClickListener {
        void onReportClick(OverloadReportEntity report);
    }

    private final List<OverloadReportEntity> reportList = new ArrayList<>();
    private final OnReportClickListener clickListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    public ReportAdapter(OnReportClickListener listener) {
        this.clickListener = listener;
    }

    public void setReports(List<OverloadReportEntity> reports) {
        reportList.clear();
        if (reports != null) {
            reportList.addAll(reports);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        OverloadReportEntity report = reportList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvVesselNumber.setText(report.getVesselNumber() != null ? report.getVesselNumber() : "Unknown Vessel");

        String timeStr = dateFormat.format(new Date(report.getTimestamp()));
        String locStr = String.format(Locale.US, "%s | GPS: %.4f° N, %.4f° E", timeStr, report.getLatitude(), report.getLongitude());
        holder.tvTimestamp.setText(locStr);

        // SYLLABUS: Sync Status Badge (Offline Queue Pattern)
        if (report.isSynced()) {
            holder.tvSyncStatus.setText(R.string.status_synced);
            holder.tvSyncStatus.setBackgroundResource(R.drawable.bg_chip_safe);
            holder.tvSyncStatus.setTextColor(ContextCompat.getColor(context, R.color.status_safe_text));
        } else {
            holder.tvSyncStatus.setText(R.string.status_pending_sync);
            holder.tvSyncStatus.setBackgroundResource(R.drawable.bg_chip_flagged);
            holder.tvSyncStatus.setTextColor(ContextCompat.getColor(context, R.color.status_danger_text));
        }

        // Crowd Severity
        String crowdLevel = report.getCrowdLevel();
        if ("CRITICAL_OVERLOAD".equals(crowdLevel)) {
            holder.tvCrowdLevel.setText(R.string.crowd_critical);
            holder.tvCrowdLevel.setTextColor(ContextCompat.getColor(context, R.color.status_danger_text));
        } else if ("MODERATE".equals(crowdLevel)) {
            holder.tvCrowdLevel.setText(R.string.crowd_moderate);
            holder.tvCrowdLevel.setTextColor(ContextCompat.getColor(context, R.color.secondary_variant_amber));
        } else {
            holder.tvCrowdLevel.setText(R.string.crowd_normal);
            holder.tvCrowdLevel.setTextColor(ContextCompat.getColor(context, R.color.status_safe_text));
        }

        holder.tvPassengerCount.setText(String.format(Locale.US, "Estimated: ~%,d Passengers", report.getEstimatedPassengerCount()));
        holder.tvAnonymous.setText(report.isAnonymous() ? "🔒 Submitted Anonymously" : "👤 Verified Passenger Report");

        // Photo Thumbnail
        Bitmap bitmap = ImageCompressionHelper.decodeBase64ToBitmap(report.getPhotoBase64());
        if (bitmap != null) {
            holder.ivThumbnail.setImageBitmap(bitmap);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.ic_shoreline_logo);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onReportClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        final TextView tvVesselNumber;
        final TextView tvTimestamp;
        final TextView tvSyncStatus;
        final TextView tvCrowdLevel;
        final TextView tvPassengerCount;
        final TextView tvAnonymous;
        final ImageView ivThumbnail;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVesselNumber = itemView.findViewById(R.id.tvReportVesselNumber);
            tvTimestamp = itemView.findViewById(R.id.tvReportTimestamp);
            tvSyncStatus = itemView.findViewById(R.id.tvReportSyncStatus);
            tvCrowdLevel = itemView.findViewById(R.id.tvReportCrowdLevel);
            tvPassengerCount = itemView.findViewById(R.id.tvReportPassengerCount);
            tvAnonymous = itemView.findViewById(R.id.tvReportAnonymous);
            ivThumbnail = itemView.findViewById(R.id.ivReportThumbnail);
        }
    }
}
