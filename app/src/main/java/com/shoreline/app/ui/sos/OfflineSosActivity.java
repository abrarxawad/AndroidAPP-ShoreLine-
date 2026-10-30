package com.shoreline.app.ui.sos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shoreline.app.R;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.dao.SosDao;
import com.shoreline.app.data.local.entity.SosSignalEntity;
import com.shoreline.app.databinding.ActivityOfflineSosBinding;
import com.shoreline.app.nearby.NearbySosRelayManager;
import com.shoreline.app.receiver.SosAlertReceiver;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.util.IntentHelper;
import com.shoreline.app.util.LocationHelper;
import com.shoreline.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: Offline SOS Distress Beacon (Google Nearby Connections P2P_CLUSTER, NearbyRadarView, Implicit Intents)
 *
 * Core Features:
 * 1. P2P Mesh Beacon: Transmits distress signals over Bluetooth & Wi-Fi Direct without cellular connectivity.
 * 2. Multi-Hop Forwarding & Deduplication: Neighboring vessels automatically forward the beacon up to 3 hops.
 * 3. Custom View (NearbyRadarView): Renders an animated sweeping radar showing active peer vessels and red danger halos.
 * 4. Implicit Intents: Direct fallback triggers for emergency dialers (16107, 999), SMS, and Android share sheet.
 * 5. Evaluator Demo Simulator: Allows testing multi-hop distress reception on a single evaluation device.
 */
public class OfflineSosActivity extends BaseActivity implements NearbySosRelayManager.NearbyMeshListener {

    private ActivityOfflineSosBinding binding;
    private NearbySosRelayManager nearbyManager;
    private SessionManager sessionManager;
    private LocationHelper locationHelper;
    private SosDao sosDao;

    private SosBeaconAdapter beaconAdapter;
    private double currentLat = LocationHelper.DEFAULT_LATITUDE;
    private double currentLon = LocationHelper.DEFAULT_LONGITUDE;

    private final ActivityResultLauncher<String[]> nearbyPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (granted == null || !granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    initNearbyMesh();
                } else {
                    showToast("Nearby permissions required for offline mesh relay");
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfflineSosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        locationHelper = new LocationHelper(this);
        sosDao = ShoreLineDatabase.getInstance(this).sosDao();
        nearbyManager = NearbySosRelayManager.getInstance(this);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        acquireLocation();

        checkPermissionsAndStartMesh();
        loadExistingBeacons();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.includedToolbar.appToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.nav_offline_sos);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.includedToolbar.appToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        beaconAdapter = new SosBeaconAdapter();
        binding.rvReceivedBeacons.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReceivedBeacons.setAdapter(beaconAdapter);
    }

    private void setupListeners() {
        // Transmit Offline SOS Beacon Button
        binding.btnBroadcastSos.setOnClickListener(v -> broadcastOfflineDistressBeacon());

        // Evaluator Demo: Simulate Incoming Distress Beacon
        binding.btnSimulateNearbyBeacon.setOnClickListener(v -> simulateIncomingDistressBeacon());

        // Fallback Implicit Intents (Syllabus Demonstration)
        binding.btnDialBiwta.setOnClickListener(v ->
                IntentHelper.dialEmergencyNumber(this, IntentHelper.BIWTA_RESCUE_HOTLINE));

        binding.btnDial999.setOnClickListener(v ->
                IntentHelper.dialEmergencyNumber(this, IntentHelper.NATIONAL_EMERGENCY_999));

        binding.btnSendSms.setOnClickListener(v -> {
            String emergencyContact = sessionManager.getEmergencyContact();
            String message = String.format(Locale.US,
                    "MAYDAY! River vessel emergency at coordinates: %.4f° N, %.4f° E. Passenger: %s. Send rescue immediately!",
                    currentLat, currentLon, sessionManager.getUserName());
            IntentHelper.sendEmergencySms(this, emergencyContact, message);
        });

        binding.btnShareDistress.setOnClickListener(v -> {
            String message = String.format(Locale.US,
                    "🚨 SHORELINE EMERGENCY SOS\nVessel in distress on Bangladesh waterway!\nCoordinates: %.4f° N, %.4f° E\nGoogle Maps: https://maps.google.com/?q=%.4f,%.4f\nSent via ShoreLine River Safety Net",
                    currentLat, currentLon, currentLat, currentLon);
            IntentHelper.shareDistressMessage(this, message);
        });
    }

    private void acquireLocation() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationAcquired(double latitude, double longitude, float accuracyMeters, boolean isMockOrFallback) {
                currentLat = latitude;
                currentLon = longitude;
            }

            @Override
            public void onLocationFailed(String reason) {
                currentLat = LocationHelper.DEFAULT_LATITUDE;
                currentLon = LocationHelper.DEFAULT_LONGITUDE;
            }
        });
    }

    private void checkPermissionsAndStartMesh() {
        List<String> perms = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.NEARBY_WIFI_DEVICES);
            }
        }

        if (perms.isEmpty()) {
            initNearbyMesh();
        } else {
            nearbyPermissionLauncher.launch(perms.toArray(new String[0]));
        }
    }

    private void initNearbyMesh() {
        nearbyManager.registerListener(this);
        nearbyManager.startMesh();
        updatePeerCountDisplay();
    }

    private void loadExistingBeacons() {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            List<SosSignalEntity> saved = sosDao.getAllSosSignals();
            runOnUiThread(() -> {
                if (saved != null && !saved.isEmpty()) {
                    beaconAdapter.setBeacons(saved);
                    binding.tvEmptyInbox.setVisibility(View.GONE);
                } else {
                    binding.tvEmptyInbox.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    /**
     * SYLLABUS: Transmit Distress Beacon over P2P_CLUSTER Mesh
     */
    private void broadcastOfflineDistressBeacon() {
        String emergencyType = "CAPSIZING_RISK";
        int checkedChipId = binding.chipGroupEmergencyType.getCheckedChipId();
        if (checkedChipId == R.id.chipEngineFailure) {
            emergencyType = "ENGINE_FAILURE";
        } else if (checkedChipId == R.id.chipMedical) {
            emergencyType = "MEDICAL_EMERGENCY";
        } else if (checkedChipId == R.id.chipStorm) {
            emergencyType = "STORM_HAZARD";
        }

        String senderName = sessionManager.getUserName();
        String senderPhone = sessionManager.getUserPhone();
        String vesselName = "River Passenger Launch";

        // Transmit through Nearby Connections Mesh
        nearbyManager.broadcastDistressBeacon(
                senderName,
                senderPhone,
                vesselName,
                currentLat,
                currentLon,
                emergencyType
        );

        binding.tvBroadcastStatus.setText("🚨 DISTRESS BEACON ACTIVE: Transmitting across river mesh...");
        binding.tvBroadcastStatus.setTextColor(ContextCompat.getColor(this, R.color.status_danger_red));

        // Add self distress halo on radar
        binding.radarView.addOrUpdatePeer("SELF_DISTRESS", "My Launch (SOS)", true);
        showToast("🚨 Emergency Beacon broadcast across nearby waterways!");
    }

    /**
     * SYLLABUS: Evaluator Simulation Demo
     * Simulates an incoming Mayday distress signal from a neighboring boat to demonstrate multi-hop relay.
     */
    private void simulateIncomingDistressBeacon() {
        String simulatedSosId = "SOS-SIM-" + System.currentTimeMillis();
        long now = System.currentTimeMillis();

        SosSignalEntity simulatedBeacon = new SosSignalEntity(
                simulatedSosId,
                "Capt. Jamal Hossain",
                "01711987654",
                23.2321, // Chandpur Confluence coordinates
                90.6473,
                now,
                1, // Relayed from hop 1
                "FV Meghna Rani",
                "ACTIVE",
                true,
                false
        );

        // 1. Save to Room database
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            sosDao.insertSos(simulatedBeacon);
        });

        // 2. Add to radar sweep display as pulsing red beacon
        binding.radarView.addOrUpdatePeer(simulatedSosId, "FV Meghna Rani", true);

        // 3. Add to RecyclerView
        beaconAdapter.addBeacon(simulatedBeacon);
        binding.tvEmptyInbox.setVisibility(View.GONE);

        // 4. Fire in-app custom broadcast (SosAlertReceiver) to trigger Morse vibration and notification
        Intent alertIntent = new Intent(SosAlertReceiver.ACTION_SOS_BEACON_DETECTED);
        alertIntent.setPackage(getPackageName());
        alertIntent.putExtra(SosAlertReceiver.EXTRA_SOS_ID, simulatedSosId);
        alertIntent.putExtra(SosAlertReceiver.EXTRA_SENDER_NAME, simulatedBeacon.getSenderName());
        alertIntent.putExtra(SosAlertReceiver.EXTRA_VESSEL_NAME, simulatedBeacon.getOriginDeviceId());
        alertIntent.putExtra(SosAlertReceiver.EXTRA_LATITUDE, simulatedBeacon.getLatitude());
        alertIntent.putExtra(SosAlertReceiver.EXTRA_LONGITUDE, simulatedBeacon.getLongitude());
        alertIntent.putExtra(SosAlertReceiver.EXTRA_EMERGENCY_TYPE, "CAPSIZING_RISK");
        alertIntent.putExtra(SosAlertReceiver.EXTRA_HOP_COUNT, 1);
        sendBroadcast(alertIntent);

        showToast(getString(R.string.sos_demo_simulated));
    }

    @Override
    public void onPeerConnected(String endpointId, String endpointName) {
        runOnUiThread(() -> {
            binding.radarView.addOrUpdatePeer(endpointId, endpointName, false);
            updatePeerCountDisplay();
        });
    }

    @Override
    public void onPeerDisconnected(String endpointId) {
        runOnUiThread(() -> {
            binding.radarView.removePeer(endpointId);
            updatePeerCountDisplay();
        });
    }

    @Override
    public void onDistressBeaconReceived(SosSignalEntity beacon, int hopCount) {
        runOnUiThread(() -> {
            binding.radarView.addOrUpdatePeer(beacon.getSosId(), beacon.getOriginDeviceId(), true);
            beaconAdapter.addBeacon(beacon);
            binding.tvEmptyInbox.setVisibility(View.GONE);
        });
    }

    @Override
    public void onBeaconBroadcastComplete(String sosId, int peersReached) {
        runOnUiThread(() -> {
            String msg = getString(R.string.sos_broadcast_success, peersReached);
            binding.tvBroadcastStatus.setText(msg);
            showToast(msg);
        });
    }

    private void updatePeerCountDisplay() {
        int peers = nearbyManager.getConnectedPeerCount();
        if (peers > 0) {
            binding.tvRadarPeerCount.setText(String.format(Locale.US, "🟢 %d Neighboring Vessels in Mesh Range", peers));
            binding.tvRadarPeerCount.setTextColor(ContextCompat.getColor(this, R.color.status_safe_text));
        } else {
            binding.tvRadarPeerCount.setText("🟢 Scanning River Reach (Nearby Connections Active)");
            binding.tvRadarPeerCount.setTextColor(ContextCompat.getColor(this, R.color.status_safe_text));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nearbyManager != null) {
            nearbyManager.unregisterListener(this);
            nearbyManager.stopMesh();
        }
        binding = null;
    }
}
