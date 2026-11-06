package com.shoreline.app.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * SYLLABUS: Multi-Source Real-Time GPS & Network Geolocation Engine
 * 1. Queries Android LocationManager (Network & GPS providers)
 * 2. Uses FusedLocationProviderClient with active live updates
 * 3. Never hardcodes stale coordinates when real GPS is active
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";

    public static final double DEFAULT_LATITUDE = 23.7045;
    public static final double DEFAULT_LONGITUDE = 90.4131;

    public interface LocationResultCallback {
        void onLocationAcquired(double latitude, double longitude, float accuracyMeters, boolean isMockOrFallback);
        void onLocationFailed(String reason);
    }

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationManager locationManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);
        this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationEnabled() {
        if (locationManager == null) return false;
        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(LocationResultCallback callback) {
        if (!hasLocationPermission()) {
            mainHandler.post(() -> callback.onLocationFailed("Location permission not granted"));
            return;
        }

        if (!isLocationEnabled()) {
            mainHandler.post(() -> callback.onLocationFailed("Device location services (GPS) are turned off in settings"));
            return;
        }

        final boolean[] locationDelivered = {false};

        // Step 1: Check instant system cached locations (Network & GPS)
        try {
            Location netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location bestLastLoc = null;
            if (netLoc != null && gpsLoc != null) {
                bestLastLoc = (gpsLoc.getTime() > netLoc.getTime()) ? gpsLoc : netLoc;
            } else if (gpsLoc != null) {
                bestLastLoc = gpsLoc;
            } else if (netLoc != null) {
                bestLastLoc = netLoc;
            }

            // If a recent location (within 2 hours) exists, deliver it immediately
            if (bestLastLoc != null && (System.currentTimeMillis() - bestLastLoc.getTime() < 2 * 60 * 60 * 1000L)) {
                locationDelivered[0] = true;
                final Location loc = bestLastLoc;
                mainHandler.post(() -> callback.onLocationAcquired(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(), false));
            }
        } catch (Exception ignored) {}

        // Step 2: Request active live update from FusedLocationClient (forces device to acquire satellites & cell towers right now)
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdates(3)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location loc = locationResult.getLastLocation();
                if (loc != null) {
                    locationDelivered[0] = true;
                    fusedLocationClient.removeLocationUpdates(this);
                    mainHandler.post(() -> callback.onLocationAcquired(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy(), false));
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } catch (Exception e) {
            Log.w(TAG, "Fused location request failed: " + e.getMessage());
        }

        // Step 3: Register native Android LocationManager listener as parallel backup (crucial for Honor MagicOS/EMUI)
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        if (!locationDelivered[0]) {
                            locationDelivered[0] = true;
                            mainHandler.post(() -> callback.onLocationAcquired(location.getLatitude(), location.getLongitude(), location.getAccuracy(), false));
                        }
                    }
                    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override public void onProviderEnabled(@NonNull String provider) {}
                    @Override public void onProviderDisabled(@NonNull String provider) {}
                }, Looper.getMainLooper());
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        if (!locationDelivered[0]) {
                            locationDelivered[0] = true;
                            mainHandler.post(() -> callback.onLocationAcquired(location.getLatitude(), location.getLongitude(), location.getAccuracy(), false));
                        }
                    }
                    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                    @Override public void onProviderEnabled(@NonNull String provider) {}
                    @Override public void onProviderDisabled(@NonNull String provider) {}
                }, Looper.getMainLooper());
            }
        } catch (Exception e) {
            Log.w(TAG, "Native LocationManager update failed: " + e.getMessage());
        }

        // Step 4: Fallback timeout safety (10 seconds)
        mainHandler.postDelayed(() -> {
            if (!locationDelivered[0]) {
                // If live GPS still searching, try fused lastLocation as last resort
                fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> {
                    if (lastLoc != null) {
                        callback.onLocationAcquired(lastLoc.getLatitude(), lastLoc.getLongitude(), lastLoc.getAccuracy(), false);
                    } else {
                        callback.onLocationFailed("Searching for GPS satellites in Khulna... Please ensure device Location is ON.");
                    }
                }).addOnFailureListener(e -> callback.onLocationFailed(e.getMessage()));
            }
        }, 8000);
    }
}
