package com.shoreline.app.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.shoreline.app.R;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.entity.ActiveJourneyEntity;
import com.shoreline.app.data.model.GhatNode;
import com.shoreline.app.data.remote.LiveTrackingManager;
import com.shoreline.app.ui.tracker.FamilyTrackerActivity;
import com.shoreline.app.util.GhatXmlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: Foreground Service & Bound Service Architecture (Location Telemetry)
 *
 * 1. Foreground Service: Runs continuous high-accuracy GPS tracking with a persistent notification,
 *    preventing the Android OS from killing the telemetry engine mid-river during long voyages.
 * 2. Bound Service: Provides an IBinder interface (LocalBinder) so FamilyTrackerActivity can attach
 *    and display real-time speed, distance, and nearest ghat without wasteful polling.
 * 3. Android 14+ Compliance: Explicitly declares and passes FOREGROUND_SERVICE_TYPE_LOCATION.
 */
public class JourneyTrackingService extends Service {

    private static final String TAG = "JourneyTrackingService";
    public static final String CHANNEL_ID = "shoreline_journey_tracking_channel";
    public static final int NOTIFICATION_ID = 5001;

    public static final String ACTION_START_TRACKING = "com.shoreline.app.action.START_TRACKING";
    public static final String ACTION_STOP_TRACKING = "com.shoreline.app.action.STOP_TRACKING";

    public static final String EXTRA_JOURNEY_CODE = "extra_journey_code";
    public static final String EXTRA_VESSEL_NAME = "extra_vessel_name";
    public static final String EXTRA_ORIGIN_ID = "extra_origin_id";
    public static final String EXTRA_ORIGIN_NAME = "extra_origin_name";
    public static final String EXTRA_DEST_ID = "extra_dest_id";
    public static final String EXTRA_DEST_NAME = "extra_dest_name";
    public static final String EXTRA_PASSENGER_NAME = "extra_passenger_name";
    public static final String EXTRA_PASSENGER_PHONE = "extra_passenger_phone";
    public static final String EXTRA_EMERGENCY_PHONE = "extra_emergency_phone";
    public static final String EXTRA_START_LAT = "extra_start_lat";
    public static final String EXTRA_START_LON = "extra_start_lon";

    public interface JourneyUpdateListener {
        void onTelemetryUpdated(@NonNull Location location, double speedKmh,
                                double totalDistanceKm, @NonNull String nearestGhat);
        void onTrackingStopped();
    }

    private final IBinder binder = new LocalBinder();
    private final List<JourneyUpdateListener> listeners = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastKnownLocation;

    private ActiveJourneyEntity activeJourney;
    private double totalDistanceKm = 0.0;
    private GhatXmlParser.RiverNetworkData networkData;
    private LiveTrackingManager liveTrackingManager;

    public class LocalBinder extends Binder {
        public JourneyTrackingService getService() {
            return JourneyTrackingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        networkData = GhatXmlParser.parseRiverNetwork(this);
        liveTrackingManager = LiveTrackingManager.getInstance(this);

        setupLocationCallback();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String action = intent.getAction();
        if (ACTION_START_TRACKING.equals(action)) {
            handleStartTracking(intent);
        } else if (ACTION_STOP_TRACKING.equals(action)) {
            handleStopTracking();
        }

        return START_STICKY;
    }

    private void handleStartTracking(Intent intent) {
        String journeyCode = intent.getStringExtra(EXTRA_JOURNEY_CODE);
        if (journeyCode == null || journeyCode.trim().isEmpty()) {
            stopSelf();
            return;
        }

        String vessel = intent.getStringExtra(EXTRA_VESSEL_NAME);
        String originId = intent.getStringExtra(EXTRA_ORIGIN_ID);
        String originName = intent.getStringExtra(EXTRA_ORIGIN_NAME);
        String destId = intent.getStringExtra(EXTRA_DEST_ID);
        String destName = intent.getStringExtra(EXTRA_DEST_NAME);
        String passName = intent.getStringExtra(EXTRA_PASSENGER_NAME);
        String passPhone = intent.getStringExtra(EXTRA_PASSENGER_PHONE);
        String emPhone = intent.getStringExtra(EXTRA_EMERGENCY_PHONE);

        double startLat = intent.getDoubleExtra(EXTRA_START_LAT, 23.7045);
        double startLon = intent.getDoubleExtra(EXTRA_START_LON, 90.4131);

        totalDistanceKm = 0.0;
        lastKnownLocation = null;

        activeJourney = new ActiveJourneyEntity(
                journeyCode,
                "",
                passName != null ? passName : "Passenger",
                passPhone != null ? passPhone : "",
                emPhone != null ? emPhone : "",
                vessel != null ? vessel : "River Launch",
                originId != null ? originId : "G01",
                originName != null ? originName : "Origin",
                destId != null ? destId : "G03",
                destName != null ? destName : "Destination",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "ACTIVE",
                startLat,
                startLon,
                0.0,
                0.0,
                originName != null ? originName : "Sadarghat",
                false
        );

        // Start Foreground with Location Type (Android 14+ compatible)
        Notification notification = buildNotification("Initializing GPS fix for river journey...", 0.0, activeJourney.getNearestGhatName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        // Publish initial journey state to Firebase immediately so voyage node is live
        liveTrackingManager.publishLocationUpdate(activeJourney, 0.0, 0.0, activeJourney.getNearestGhatName());

        startLocationUpdates();
    }

    private void handleStopTracking() {
        stopLocationUpdates();

        if (activeJourney != null) {
            activeJourney.setStatus("STOPPED");
            liveTrackingManager.markJourneyEnded(activeJourney.getJourneyCode());
        }

        for (JourneyUpdateListener listener : listeners) {
            listener.onTrackingStopped();
        }

        stopForeground(true);
        stopSelf();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 4000)
                .setMinUpdateIntervalMillis(2000)
                .setMinUpdateDistanceMeters(3.0f)
                .build();

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            Log.d(TAG, "Continuous FusedLocation tracking initiated.");
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission missing for foreground tracking: " + e.getMessage());
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location == null || activeJourney == null) return;

                // 1. Calculate Delta Distance
                if (lastKnownLocation != null) {
                    float distanceMeters = lastKnownLocation.distanceTo(location);
                    // Filter out GPS jitter (< 2 meters)
                    if (distanceMeters > 2.0f) {
                        totalDistanceKm += (distanceMeters / 1000.0);
                    }
                }
                lastKnownLocation = location;

                // 2. Compute Speed in km/h
                double speedKmh = location.hasSpeed() ? (location.getSpeed() * 3.6) : 0.0;

                // 3. Find Nearest Ghat on Bangladesh River Network
                String nearestGhat = findNearestGhatName(location.getLatitude(), location.getLongitude());

                // 4. Update Model State & Broadcast
                activeJourney.setCurrentLatitude(location.getLatitude());
                activeJourney.setCurrentLongitude(location.getLongitude());

                liveTrackingManager.publishLocationUpdate(activeJourney, speedKmh, totalDistanceKm, nearestGhat);

                // 5. Update Foreground Notification
                updateNotification(speedKmh, nearestGhat);

                // 6. Notify Bound UI Listeners
                for (JourneyUpdateListener listener : listeners) {
                    listener.onTelemetryUpdated(location, speedKmh, totalDistanceKm, nearestGhat);
                }
            }
        };
    }

    private String findNearestGhatName(double lat, double lon) {
        if (networkData == null || networkData.ghatMap.isEmpty()) return "Mid-River";
        String nearestName = "En Route";
        double minDistance = Double.MAX_VALUE;

        for (GhatNode ghat : networkData.ghatMap.values()) {
            float[] results = new float[1];
            Location.distanceBetween(lat, lon, ghat.getLatitude(), ghat.getLongitude(), results);
            if (results[0] < minDistance) {
                minDistance = results[0];
                nearestName = ghat.getName();
            }
        }
        return nearestName;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "River Journey Live Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Continuous GPS tracking and family location sharing for waterway journeys");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification(String statusText, double speedKmh, String nearestGhat) {
        Intent notificationIntent = new Intent(this, FamilyTrackerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, JourneyTrackingService.class);
        stopIntent.setAction(ACTION_STOP_TRACKING);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String title = (activeJourney != null)
                ? String.format(Locale.US, "⛵ Voyage to %s (%s)", activeJourney.getDestGhatName(), activeJourney.getJourneyCode())
                : "⛵ River Journey Active";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(statusText)
                .setSmallIcon(R.drawable.ic_shoreline_logo)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Tracking", stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void updateNotification(double speedKmh, String nearestGhat) {
        if (activeJourney == null) return;
        String content = String.format(Locale.US, "Speed: %.1f km/h | Near: %s | Distance: %.1f km",
                speedKmh, nearestGhat, totalDistanceKm);
        Notification notification = buildNotification(content, speedKmh, nearestGhat);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    public void registerListener(JourneyUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            // Immediately dispatch last known state if available
            if (lastKnownLocation != null && activeJourney != null) {
                listener.onTelemetryUpdated(
                        lastKnownLocation,
                        activeJourney.getCurrentSpeedKmh(),
                        totalDistanceKm,
                        activeJourney.getNearestGhatName()
                );
            }
        }
    }

    public void unregisterListener(JourneyUpdateListener listener) {
        listeners.remove(listener);
    }

    public ActiveJourneyEntity getActiveJourney() {
        return activeJourney;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        listeners.clear();
        Log.d(TAG, "JourneyTrackingService destroyed.");
    }
}
