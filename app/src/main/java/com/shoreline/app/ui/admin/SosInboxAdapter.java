package com.shoreline.app.ui.admin;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.shoreline.app.R;
import com.shoreline.app.data.local.entity.SosSignalEntity;
import com.shoreline.app.databinding.ItemAdminIncidentBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: RecyclerView 4 - SOS Inbox & Admin Incident Logs
 *
 * Implements:
 * 1. Custom ViewHolder pattern with ViewBinding.
 * 2. Long-press context menu and 3-dot overflow PopupMenu.
 * 3. Dynamic status updating (ACTIVE -> ACKNOWLEDGED -> RESOLVED).
 * 4. Swipe-to-action gesture integration support via ItemTouchHelper.
 */
public class SosInboxAdapter extends RecyclerView.Adapter<SosInboxAdapter.IncidentViewHolder> {

    public interface OnIncidentActionListener {
        void onCallVessel(SosSignalEntity incident);
        void onViewOnMap(SosSignalEntity incident);
        void onStatusChange(SosSignalEntity incident, String newStatus);
        void onDeleteIncident(SosSignalEntity incident);
        void onIncidentLongClick(SosSignalEntity incident);
    }

    private final List<SosSignalEntity> incidents = new ArrayList<>();
    private OnIncidentActionListener actionListener;

    public void setOnIncidentActionListener(OnIncidentActionListener listener) {
        this.actionListener = listener;
    }

    public void setIncidents(List<SosSignalEntity> newIncidents) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return incidents.size();
            }

            @Override
            public int getNewListSize() {
                return newIncidents != null ? newIncidents.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return incidents.get(oldItemPosition).getSosId().equals(
                        newIncidents.get(newItemPosition).getSosId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                SosSignalEntity oldItem = incidents.get(oldItemPosition);
                SosSignalEntity newItem = newIncidents.get(newItemPosition);
                return oldItem.getStatus().equals(newItem.getStatus())
                        && oldItem.getHopCount() == newItem.getHopCount();
            }
        });

        incidents.clear();
        if (newIncidents != null) {
            incidents.addAll(newIncidents);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    public SosSignalEntity getItemAt(int position) {
        if (position >= 0 && position < incidents.size()) {
            return incidents.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminIncidentBinding binding = ItemAdminIncidentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new IncidentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int position) {
        holder.bind(incidents.get(position));
    }

    @Override
    public int getItemCount() {
        return incidents.size();
    }

    class IncidentViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminIncidentBinding binding;

        IncidentViewHolder(ItemAdminIncidentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SosSignalEntity incident) {
            Context context = itemView.getContext();

            // Sender & Vessel Name
            String vessel = incident.getOriginDeviceId();
            if (vessel == null || vessel.trim().isEmpty()) {
                vessel = "River Launch (P2P Mesh)";
            }
            binding.tvVesselName.setText(String.format(Locale.US, "%s • %s",
                    vessel, incident.getSenderName()));

            // Timestamp (With clear time icon)
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    incident.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            binding.tvIncidentTime.setText("🕒 " + relativeTime);

            // Coordinates
            binding.tvCoordinates.setText(String.format(Locale.US,
                    "📍 Location: %.4f° N, %.4f° E",
                    incident.getLatitude(), incident.getLongitude()));

            // Hop Count Mesh Badge
            if (incident.getHopCount() <= 0) {
                binding.tvHopBadge.setText("📡 Direct Signal");
            } else {
                binding.tvHopBadge.setText(String.format(Locale.US, "📡 Hop %d/3 (Relayed)", incident.getHopCount()));
            }

            // Status chip and theme styling
            String status = incident.getStatus() != null ? incident.getStatus() : "ACTIVE";
            if ("RESOLVED".equalsIgnoreCase(status) || "RESCUED".equalsIgnoreCase(status)) {
                binding.tvIncidentStatus.setText("✓ RESOLVED / SAFE");
                binding.tvIncidentStatus.setBackgroundResource(R.drawable.bg_chip_safe);
                binding.tvIncidentStatus.setTextColor(ContextCompat.getColor(context, R.color.status_safe_text));
                binding.cardIncident.setStrokeColor(ContextCompat.getColor(context, R.color.status_safe_green));
                binding.tvEmergencyNature.setTextColor(ContextCompat.getColor(context, R.color.status_safe_text));
                binding.tvEmergencyNature.setText("Rescue operations concluded. Vessel secured.");
            } else if ("ACKNOWLEDGED".equalsIgnoreCase(status)) {
                binding.tvIncidentStatus.setText("⏳ ACKNOWLEDGED / DISPATCHED");
                binding.tvIncidentStatus.setBackgroundResource(R.drawable.bg_chip_warning);
                binding.tvIncidentStatus.setTextColor(ContextCompat.getColor(context, R.color.status_warning_text));
                binding.cardIncident.setStrokeColor(ContextCompat.getColor(context, R.color.status_warning_amber));
                binding.tvEmergencyNature.setTextColor(ContextCompat.getColor(context, R.color.status_warning_text));
                binding.tvEmergencyNature.setText("BIWTA Rescue Patrol en route to GPS sector.");
            } else {
                binding.tvIncidentStatus.setText("🚨 ACTIVE DISTRESS");
                binding.tvIncidentStatus.setBackgroundResource(R.drawable.bg_chip_flagged);
                binding.tvIncidentStatus.setTextColor(ContextCompat.getColor(context, R.color.status_danger_text));
                binding.cardIncident.setStrokeColor(ContextCompat.getColor(context, R.color.status_danger_red));
                binding.tvEmergencyNature.setTextColor(ContextCompat.getColor(context, R.color.status_danger_red));
                binding.tvEmergencyNature.setText("⚠️ Immediate Port Authority intervention required!");
            }

            // Button actions
            binding.btnCallVessel.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onCallVessel(incident);
                }
            });

            binding.btnViewOnMap.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onViewOnMap(incident);
                }
            });

            // PopupMenu (Contextual actions)
            binding.btnIncidentMenu.setOnClickListener(v -> showIncidentPopupMenu(v, incident));

            // Syllabus requirement: Long-press context action
            binding.getRoot().setOnLongClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onIncidentLongClick(incident);
                }
                showIncidentPopupMenu(v, incident);
                return true;
            });
        }

        private void showIncidentPopupMenu(View anchor, SosSignalEntity incident) {
            PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
            popup.getMenu().add(0, 1, 0, "📞 Call Vessel Master");
            popup.getMenu().add(0, 2, 1, "🗺️ View on River Map");
            if (!"ACKNOWLEDGED".equalsIgnoreCase(incident.getStatus())) {
                popup.getMenu().add(0, 3, 2, "⏳ Mark Acknowledged (Dispatch Patrol)");
            }
            if (!"RESOLVED".equalsIgnoreCase(incident.getStatus())) {
                popup.getMenu().add(0, 4, 3, "✓ Mark Resolved (Rescue Complete)");
            }
            popup.getMenu().add(0, 5, 4, "🗑️ Archive / Dismiss Incident");

            popup.setOnMenuItemClickListener(item -> {
                if (actionListener == null) return false;
                switch (item.getItemId()) {
                    case 1:
                        actionListener.onCallVessel(incident);
                        return true;
                    case 2:
                        actionListener.onViewOnMap(incident);
                        return true;
                    case 3:
                        actionListener.onStatusChange(incident, "ACKNOWLEDGED");
                        return true;
                    case 4:
                        actionListener.onStatusChange(incident, "RESOLVED");
                        return true;
                    case 5:
                        actionListener.onDeleteIncident(incident);
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        }
    }
}

