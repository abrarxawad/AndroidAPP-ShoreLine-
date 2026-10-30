package com.shoreline.app.ui.sos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shoreline.app.data.local.entity.SosSignalEntity;
import com.shoreline.app.databinding.ItemSosBeaconBinding;
import com.shoreline.app.util.IntentHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: RecyclerView Adapter - Received Mesh Distress Beacons
 * Renders received emergency beacons from neighboring vessels with one-tap dialer and map triggers.
 */
public class SosBeaconAdapter extends RecyclerView.Adapter<SosBeaconAdapter.BeaconViewHolder> {

    private final List<SosSignalEntity> beaconList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    public void setBeacons(List<SosSignalEntity> beacons) {
        this.beaconList.clear();
        if (beacons != null) {
            this.beaconList.addAll(beacons);
        }
        notifyDataSetChanged();
    }

    public void addBeacon(SosSignalEntity beacon) {
        if (beacon == null) return;
        // Avoid duplicate in UI
        for (int i = 0; i < beaconList.size(); i++) {
            if (beaconList.get(i).getSosId().equals(beacon.getSosId())) {
                beaconList.set(i, beacon);
                notifyItemChanged(i);
                return;
            }
        }
        beaconList.add(0, beacon);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public BeaconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSosBeaconBinding binding = ItemSosBeaconBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BeaconViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconViewHolder holder, int position) {
        holder.bind(beaconList.get(position));
    }

    @Override
    public int getItemCount() {
        return beaconList.size();
    }

    class BeaconViewHolder extends RecyclerView.ViewHolder {
        private final ItemSosBeaconBinding binding;

        BeaconViewHolder(ItemSosBeaconBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SosSignalEntity beacon) {
            binding.tvBeaconVessel.setText(String.format("🚨 Vessel: %s", beacon.getOriginDeviceId()));

            int hops = beacon.getHopCount();
            if (hops == 0) {
                binding.tvBeaconHopBadge.setText("DIRECT (HOP 0)");
            } else {
                binding.tvBeaconHopBadge.setText(String.format(Locale.US, "RELAYED (HOP %d/3)", hops));
            }

            binding.tvBeaconSenderInfo.setText(String.format("Sender: %s • Phone: %s",
                    beacon.getSenderName() != null ? beacon.getSenderName() : "Unknown Passenger",
                    beacon.getSenderPhone() != null ? beacon.getSenderPhone() : "N/A"));

            String timeStr = timeFormat.format(new Date(beacon.getTimestamp()));
            binding.tvBeaconCoordinates.setText(String.format(Locale.US,
                    "GPS: %.4f° N, %.4f° E • Received at %s",
                    beacon.getLatitude(), beacon.getLongitude(), timeStr));

            binding.btnCallSender.setOnClickListener(v -> {
                String phone = beacon.getSenderPhone();
                if (phone != null && !phone.isEmpty()) {
                    IntentHelper.dialEmergencyNumber(v.getContext(), phone);
                } else {
                    IntentHelper.dialEmergencyNumber(v.getContext(), IntentHelper.BIWTA_RESCUE_HOTLINE);
                }
            });

            binding.btnOpenLocation.setOnClickListener(v -> {
                IntentHelper.openGeoLocation(v.getContext(), beacon.getLatitude(), beacon.getLongitude(),
                        "Distress Vessel: " + beacon.getOriginDeviceId());
            });
        }
    }
}
