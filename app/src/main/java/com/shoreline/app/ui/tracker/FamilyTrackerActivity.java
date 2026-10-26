package com.shoreline.app.ui.tracker;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.ValueEventListener;
import com.shoreline.app.R;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.entity.ActiveJourneyEntity;
import com.shoreline.app.data.local.entity.JourneyBreadcrumbEntity;
import com.shoreline.app.data.model.GhatNode;
import com.shoreline.app.data.remote.LiveTrackingManager;
import com.shoreline.app.databinding.ActivityFamilyTrackerBinding;
import com.shoreline.app.service.JourneyTrackingService;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.util.GhatXmlParser;
import com.shoreline.app.util.JourneyCodeGenerator;
import com.shoreline.app.util.SessionManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: Family Journey Tracker Activity (Foreground Service, Bound Service, osmdroid Map, Realtime DB)
 * Core Features:
 * 1. Passenger Mode: Starts Foreground Service tracking with persistent notification, generates 6-character
 *    code (e.g., "PADMA7"), and displays live speed, distance, nearest ghat, and elapsed voyage time.
 * 2. Family Viewer Mode: Family member inputs the 6-character code to view the live boat marker,
 *    GPS telemetry, route breadcrumbs polyline on osmdroid map, and quick-call button.
 * 3. Offline Mid-River Detection: Detects when signal is lost for >15 minutes and informs the family.
 */
public class FamilyTrackerActivity extends BaseActivity implements JourneyTrackingService.JourneyUpdateListener {

    private ActivityFamilyTrackerBinding binding;
    private GhatXmlParser.RiverNetworkData networkData;
    private List<GhatNode> ghatList;
    private SessionManager sessionManager;
    private LiveTrackingManager liveTrackingManager;

    // Service Binding
    private JourneyTrackingService trackingService;
    private boolean isServiceBound = false;
    private ActiveJourneyEntity activeJourney;

    // Family Viewer Mode
    private String activeViewerCode = null;
    private ValueEventListener viewerListener = null;

    // Map Overlays
    private Marker passengerBoatMarker;
    private Polyline passengerBreadcrumbLine;
    private Marker viewerBoatMarker;
    private Polyline viewerBreadcrumbLine;

    // Voyage Timer
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private long voyageStartTime = 0;

    // Permission Launchers
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                if (fine != null && fine) {
                    startPassengerVoyage();
                } else {
                    showToast(getString(R.string.tracker_perm_required));
                }
            });

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            JourneyTrackingService.LocalBinder binder = (JourneyTrackingService.LocalBinder) service;
            trackingService = binder.getService();
            isServiceBound = true;
            trackingService.registerListener(FamilyTrackerActivity.this);

            ActiveJourneyEntity serviceJourney = trackingService.getActiveJourney();
            if (serviceJourney != null) {
                activeJourney = serviceJourney;
                showActiveVoyageUI(activeJourney);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            trackingService = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SYLLABUS: OpenStreetMap (osmdroid) Configuration
        Configuration.getInstance().load(this, getPreferences(Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityFamilyTrackerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        liveTrackingManager = LiveTrackingManager.getInstance(this);

        // Parse authentic river network XML
        networkData = GhatXmlParser.parseRiverNetwork(this);
        ghatList = new ArrayList<>(networkData.ghatMap.values());

        setupToolbar();
        setupSpinners();
        setupMaps();
        setupModeToggle();
        setupListeners();

        // Check if there is an existing active journey in Room SQLite
        checkExistingActiveJourney();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.includedToolbar.appToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.nav_family_tracker);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.includedToolbar.appToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        ArrayAdapter<GhatNode> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ghatList);

        binding.spinnerOrigin.setAdapter(adapter);
        binding.spinnerDestination.setAdapter(adapter);

        binding.spinnerOrigin.setSelection(0); // Dhaka Sadarghat
        if (ghatList.size() > 2) {
            binding.spinnerDestination.setSelection(2); // Barisal
        }
    }

    private void setupMaps() {
        configureMapView(binding.mapPassenger);
        configureMapView(binding.mapViewer);

        // Center initially on Dhaka Sadarghat launch terminal
        GeoPoint sadarghat = new GeoPoint(23.7045, 90.4131);
        binding.mapPassenger.getController().setCenter(sadarghat);
        binding.mapViewer.getController().setCenter(sadarghat);
    }

    private void configureMapView(MapView mapView) {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.getController().setZoom(11.5);
    }

    private void setupModeToggle() {
        binding.toggleModeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnTabPassenger) {
                binding.layoutPassengerMode.setVisibility(View.VISIBLE);
                binding.layoutViewerMode.setVisibility(View.GONE);
            } else if (checkedId == R.id.btnTabViewer) {
                binding.layoutPassengerMode.setVisibility(View.GONE);
                binding.layoutViewerMode.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupListeners() {
        // Start Journey Button
        binding.btnStartJourney.setOnClickListener(v -> checkPermissionsAndStartVoyage());

        // Stop Journey Button
        binding.btnStopJourney.setOnClickListener(v -> stopPassengerVoyage());

        // Copy Share Code
        binding.btnCopyCode.setOnClickListener(v -> {
            if (activeJourney != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Journey Code", activeJourney.getJourneyCode());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    showToast(getString(R.string.tracker_copy_success));
                }
            }
        });

        // Share Code via SMS / WhatsApp / ShareSheet
        binding.btnShareCode.setOnClickListener(v -> {
            if (activeJourney != null) {
                String message = getString(R.string.tracker_share_message, activeJourney.getJourneyCode());
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share Voyage Tracking Code"));
            }
        });

        // Family Viewer: Search Journey Code
        binding.btnSearchJourney.setOnClickListener(v -> {
            String code = binding.etViewerCode.getText() != null
                    ? binding.etViewerCode.getText().toString().trim().toUpperCase() : "";

            if (code.length() < 4) {
                showToast(getString(R.string.tracker_error_empty_code));
                return;
            }
            trackFamilyMemberJourney(code);
        });
    }

    private void checkExistingActiveJourney() {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            ActiveJourneyEntity existing = ShoreLineDatabase.getInstance(this).journeyDao().getActiveJourney();
            if (existing != null) {
                runOnUiThread(() -> {
                    activeJourney = existing;
                    showActiveVoyageUI(existing);

                    // Rebind service to resume receiving telemetry
                    Intent serviceIntent = new Intent(this, JourneyTrackingService.class);
                    bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                });
            }
        });
    }

    private void checkPermissionsAndStartVoyage() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (permissions.isEmpty()) {
            startPassengerVoyage();
        } else {
            locationPermissionLauncher.launch(permissions.toArray(new String[0]));
        }
    }

    /**
     * SYLLABUS: Foreground Service Activation with Intent Extras
     * Generates a 6-character voyage code and launches JourneyTrackingService.
     */
    private void startPassengerVoyage() {
        GhatNode origin = (GhatNode) binding.spinnerOrigin.getSelectedItem();
        GhatNode destination = (GhatNode) binding.spinnerDestination.getSelectedItem();
        if (origin == null || destination == null) return;

        String vesselName = binding.etVesselName.getText() != null
                ? binding.etVesselName.getText().toString().trim() : "River Launch";
        if (vesselName.isEmpty()) vesselName = "River Launch";

        // Generate memorable 6-character voyage code (e.g. "PADMA7")
        String journeyCode = JourneyCodeGenerator.generateCode();

        voyageStartTime = System.currentTimeMillis();

        activeJourney = new ActiveJourneyEntity(
                journeyCode,
                sessionManager.getUserId(),
                sessionManager.getUserName(),
                sessionManager.getUserPhone(),
                sessionManager.getEmergencyContact(),
                vesselName,
                origin.getId(),
                origin.getName(),
                destination.getId(),
                destination.getName(),
                voyageStartTime,
                voyageStartTime,
                "ACTIVE",
                origin.getLatitude(),
                origin.getLongitude(),
                0.0,
                0.0,
                origin.getName(),
                false
        );

        // Launch and Bind Foreground Service
        Intent serviceIntent = new Intent(this, JourneyTrackingService.class);
        serviceIntent.setAction(JourneyTrackingService.ACTION_START_TRACKING);
        serviceIntent.putExtra(JourneyTrackingService.EXTRA_JOURNEY_CODE, journeyCode);
        serviceIntent.putExtra(JourneyTrackingService.EXTRA_VESSEL_NAME, vesselName);
        serviceIntent.putExtra(JourneyTrackingService.EXTRA_ORIGIN_ID, origin.getId());
        serviceIntent.putExtra(JourneyTrackingService.EXTRA_ORIGIN_NAME, origin.getName());
        serviceIntent.putExtra(JourneyTrackingService.EXTRA_DEST_ID, destination.getId());
        serviceIntent.putExtra(JourneyTrackingService.EXTRA_DEST_NAME, destination.getName());
        serviceIntent.putExtra(JourneyTrackingService.EXTRA_PASSENGER_NAME, sessionManager.getUserName());
        serviceIntent.putExtra(JourneyTrackingService.EXTRA_PASSENGER_PHONE, sessionManager.getUserPhone());
        serviceIntent.putExtra(JourneyTrackingService.EXTRA_EMERGENCY_PHONE, sessionManager.getEmergencyContact());

        ContextCompat.startForegroundService(this, serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        showActiveVoyageUI(activeJourney);
    }

    private void showActiveVoyageUI(ActiveJourneyEntity journey) {
        binding.cardVoyageSetup.setVisibility(View.GONE);
        binding.cardActiveJourney.setVisibility(View.VISIBLE);

        binding.tvShareCode.setText(journey.getJourneyCode());
        binding.tvPassengerRouteHeader.setText(String.format("%s • %s ➔ %s",
                journey.getVesselName(), journey.getOriginGhatName(), journey.getDestGhatName()));

        binding.tvPassengerSpeed.setText(String.format(Locale.US, "%.1f km/h", journey.getCurrentSpeedKmh()));
        binding.tvPassengerDistance.setText(String.format(Locale.US, "%.1f km", journey.getTotalDistanceKm()));
        binding.tvPassengerNearestGhat.setText(journey.getNearestGhatName());

        voyageStartTime = journey.getStartedAt() > 0 ? journey.getStartedAt() : System.currentTimeMillis();
        startVoyageTimer();

        // Place boat marker on passenger map
        GeoPoint startPoint = new GeoPoint(journey.getCurrentLatitude(), journey.getCurrentLongitude());
        updatePassengerMap(startPoint, journey.getVesselName());
    }

    private void stopPassengerVoyage() {
        stopVoyageTimer();

        if (isServiceBound) {
            if (trackingService != null) {
                trackingService.unregisterListener(this);
            }
            unbindService(serviceConnection);
            isServiceBound = false;
        }

        Intent stopIntent = new Intent(this, JourneyTrackingService.class);
        stopIntent.setAction(JourneyTrackingService.ACTION_STOP_TRACKING);
        startService(stopIntent);

        if (activeJourney != null) {
            liveTrackingManager.markJourneyEnded(activeJourney.getJourneyCode());
            activeJourney = null;
        }

        binding.cardActiveJourney.setVisibility(View.GONE);
        binding.cardVoyageSetup.setVisibility(View.VISIBLE);
        showToast("Voyage ended. Family members notified of port arrival.");
    }

    private void startVoyageTimer() {
        stopVoyageTimer();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - voyageStartTime;
                int seconds = (int) (elapsedMillis / 1000) % 60;
                int minutes = (int) ((elapsedMillis / (1000 * 60)) % 60);
                int hours = (int) ((elapsedMillis / (1000 * 60 * 60)));

                binding.tvPassengerElapsed.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopVoyageTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerRunnable = null;
        }
    }

    /**
     * SYLLABUS: Bound Service Callback - Live Telemetry Reception
     */
    @Override
    public void onTelemetryUpdated(@NonNull Location location, double speedKmh,
                                   double totalDistanceKm, @NonNull String nearestGhat) {
        runOnUiThread(() -> {
            binding.tvPassengerSpeed.setText(String.format(Locale.US, "%.1f km/h", speedKmh));
            binding.tvPassengerDistance.setText(String.format(Locale.US, "%.1f km", totalDistanceKm));
            binding.tvPassengerNearestGhat.setText(nearestGhat);

            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
            String vessel = activeJourney != null ? activeJourney.getVesselName() : "Vessel";
            updatePassengerMap(point, vessel);
        });
    }

    @Override
    public void onTrackingStopped() {
        runOnUiThread(() -> {
            stopVoyageTimer();
            binding.cardActiveJourney.setVisibility(View.GONE);
            binding.cardVoyageSetup.setVisibility(View.VISIBLE);
        });
    }

    private void updatePassengerMap(GeoPoint point, String title) {
        if (passengerBoatMarker == null) {
            passengerBoatMarker = new Marker(binding.mapPassenger);
            passengerBoatMarker.setTitle("⛵ " + title);
            passengerBoatMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            binding.mapPassenger.getOverlays().add(passengerBoatMarker);
        }
        passengerBoatMarker.setPosition(point);

        // Add to breadcrumb line
        if (passengerBreadcrumbLine == null) {
            passengerBreadcrumbLine = new Polyline(binding.mapPassenger);
            passengerBreadcrumbLine.setColor(ContextCompat.getColor(this, R.color.primary_maritime));
            passengerBreadcrumbLine.setWidth(7.0f);
            binding.mapPassenger.getOverlays().add(0, passengerBreadcrumbLine);
        }
        passengerBreadcrumbLine.addPoint(point);

        binding.mapPassenger.getController().animateTo(point);
        binding.mapPassenger.invalidate();
    }

    /**
     * =========================================================================
     * FAMILY VIEWER MODE: Live Tracking by 6-Character Voyage Code
     * =========================================================================
     */
    private void trackFamilyMemberJourney(@NonNull String journeyCode) {
        // Remove previous listener if any
        if (activeViewerCode != null && viewerListener != null) {
            liveTrackingManager.stopTrackingJourney(activeViewerCode, viewerListener);
        }
        activeViewerCode = journeyCode;

        viewerListener = liveTrackingManager.startTrackingJourney(journeyCode, new LiveTrackingManager.JourneyTrackingCallback() {
            @Override
            public void onJourneyUpdated(@NonNull ActiveJourneyEntity journey) {
                displayViewerTelemetry(journey);
            }

            @Override
            public void onJourneyNotFound(String code) {
                showToast(getString(R.string.tracker_error_not_found, code));
                binding.cardViewerDetails.setVisibility(View.GONE);
            }

            @Override
            public void onError(String errorMessage) {
                showToast("Connection issue: " + errorMessage);
            }
        });
    }

    private void displayViewerTelemetry(ActiveJourneyEntity journey) {
        binding.cardViewerDetails.setVisibility(View.VISIBLE);
        binding.tvViewerVesselRoute.setText(String.format("%s (%s ➔ %s)",
                journey.getVesselName(), journey.getOriginGhatName(), journey.getDestGhatName()));

        binding.tvViewerPassengerInfo.setText(String.format("Passenger: %s | Share Code: %s",
                journey.getPassengerName(), journey.getJourneyCode()));

        // Check if mid-river signal was lost (> 15 minutes without GPS update)
        long timeSinceLastUpdate = System.currentTimeMillis() - journey.getLastUpdatedAt();
        boolean isSignalLost = (timeSinceLastUpdate > 15 * 60 * 1000L) && "ACTIVE".equals(journey.getStatus());

        if ("ARRIVED".equals(journey.getStatus()) || "STOPPED".equals(journey.getStatus())) {
            binding.tvViewerStatusBadge.setText(R.string.tracker_status_arrived);
            binding.tvViewerStatusBadge.setBackgroundResource(R.drawable.bg_chip_safe);
            binding.tvViewerStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_safe_text));
        } else if (isSignalLost) {
            binding.tvViewerStatusBadge.setText(R.string.tracker_status_lost);
            binding.tvViewerStatusBadge.setBackgroundResource(R.drawable.bg_chip_flagged);
            binding.tvViewerStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_danger_text));
        } else {
            binding.tvViewerStatusBadge.setText(R.string.tracker_status_active);
            binding.tvViewerStatusBadge.setBackgroundResource(R.drawable.bg_chip_safe);
            binding.tvViewerStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_safe_text));
        }

        binding.tvViewerTelemetry.setText(String.format(Locale.US, "Speed: %.1f km/h • Near %s",
                journey.getCurrentSpeedKmh(), journey.getNearestGhatName()));

        int minutesAgo = (int) (timeSinceLastUpdate / (1000 * 60));
        String updatedText = minutesAgo < 1 ? "Updated just now • Live GPS" : String.format(Locale.US, "Updated %d mins ago", minutesAgo);
        binding.tvViewerLastUpdated.setText(updatedText);

        // Configure Call Passenger Button
        String contactPhone = journey.getPassengerPhone();
        if (contactPhone != null && !contactPhone.isEmpty()) {
            binding.btnCallPassenger.setVisibility(View.VISIBLE);
            binding.btnCallPassenger.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactPhone));
                startActivity(dialIntent);
            });
        } else {
            binding.btnCallPassenger.setVisibility(View.GONE);
        }

        // Update Viewer osmdroid Map
        GeoPoint point = new GeoPoint(journey.getCurrentLatitude(), journey.getCurrentLongitude());
        updateViewerMap(point, journey.getVesselName(), journey.getJourneyCode());
    }

    private void updateViewerMap(GeoPoint point, String title, String code) {
        if (viewerBoatMarker == null) {
            viewerBoatMarker = new Marker(binding.mapViewer);
            viewerBoatMarker.setTitle(String.format("⛵ %s (%s)", title, code));
            viewerBoatMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            binding.mapViewer.getOverlays().add(viewerBoatMarker);
        }
        viewerBoatMarker.setPosition(point);

        // Load coordinate history breadcrumbs from Room
        liveTrackingManager.fetchBreadcrumbs(code, breadcrumbs -> {
            if (viewerBreadcrumbLine == null) {
                viewerBreadcrumbLine = new Polyline(binding.mapViewer);
                viewerBreadcrumbLine.setColor(ContextCompat.getColor(this, R.color.secondary_safety_orange));
                viewerBreadcrumbLine.setWidth(7.0f);
                binding.mapViewer.getOverlays().add(0, viewerBreadcrumbLine);
            }
            List<GeoPoint> points = new ArrayList<>();
            for (JourneyBreadcrumbEntity b : breadcrumbs) {
                points.add(new GeoPoint(b.getLatitude(), b.getLongitude()));
            }
            points.add(point);
            viewerBreadcrumbLine.setPoints(points);
            binding.mapViewer.invalidate();
        });

        binding.mapViewer.getController().animateTo(point);
        binding.mapViewer.invalidate();
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
    protected void onResume() {
        super.onResume();
        binding.mapPassenger.onResume();
        binding.mapViewer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapPassenger.onPause();
        binding.mapViewer.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVoyageTimer();

        if (isServiceBound) {
            if (trackingService != null) {
                trackingService.unregisterListener(this);
            }
            unbindService(serviceConnection);
            isServiceBound = false;
        }

        if (activeViewerCode != null && viewerListener != null) {
            liveTrackingManager.stopTrackingJourney(activeViewerCode, viewerListener);
        }

        binding = null;
    }
}
