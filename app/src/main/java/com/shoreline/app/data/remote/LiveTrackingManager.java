package com.shoreline.app.data.remote;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.dao.JourneyDao;
import com.shoreline.app.data.local.entity.ActiveJourneyEntity;
import com.shoreline.app.data.local.entity.JourneyBreadcrumbEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SYLLABUS: Firebase Realtime Database & Cloud Firestore - Live Telemetry Sync
 * Manages high-frequency passenger position broadcasts and allows family members to track launches in real-time.
 * Features automatic Room SQLite offline degradation when mid-river cellular connectivity is lost.
 */
public class LiveTrackingManager {

    private static final String TAG = "LiveTrackingManager";
    private static final String RTDB_JOURNEYS_NODE = "active_journeys";
    private static final String FIRESTORE_JOURNEYS_COLLECTION = "active_journeys";

    public interface JourneyTrackingCallback {
        void onJourneyUpdated(@NonNull ActiveJourneyEntity journey);
        void onJourneyNotFound(String code);
        void onError(String errorMessage);
    }

    private static volatile LiveTrackingManager INSTANCE;
    private final Context context;
    private final ShoreLineDatabase database;
    private final JourneyDao journeyDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private DatabaseReference rtdbRef;
    private FirebaseFirestore firestore;

    private LiveTrackingManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = ShoreLineDatabase.getInstance(context);
        this.journeyDao = database.journeyDao();

        try {
            this.rtdbRef = FirebaseDatabase.getInstance().getReference(RTDB_JOURNEYS_NODE);
        } catch (Exception e) {
            Log.w(TAG, "Firebase Realtime DB not initialized (using Room local persistence): " + e.getMessage());
        }

        try {
            this.firestore = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            Log.w(TAG, "Firebase Firestore not initialized: " + e.getMessage());
        }
    }

    public static LiveTrackingManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LiveTrackingManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LiveTrackingManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Publishes latest GPS telemetry for an active river voyage.
     * Persists to local Room database immediately, then pushes to Firebase Realtime DB.
     */
    public void publishLocationUpdate(@NonNull ActiveJourneyEntity journey, double speedKmh,
                                      double totalDistanceKm, String nearestGhat) {
        journey.setCurrentSpeedKmh(speedKmh);
        journey.setTotalDistanceKm(totalDistanceKm);
        journey.setNearestGhatName(nearestGhat);
        journey.setLastUpdatedAt(System.currentTimeMillis());

        // 1. Local Room DB Persistence (Guaranteed offline capability)
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            journeyDao.insertJourney(journey);

            // Record breadcrumb
            JourneyBreadcrumbEntity breadcrumb = new JourneyBreadcrumbEntity(
                    journey.getJourneyCode(),
                    journey.getCurrentLatitude(),
                    journey.getCurrentLongitude(),
                    speedKmh,
                    System.currentTimeMillis(),
                    false
            );
            journeyDao.insertBreadcrumb(breadcrumb);
        });

        // 2. Remote Firebase Realtime DB Broadcast (if online)
        try {
            if (rtdbRef != null) {
                Map<String, Object> telemetry = new HashMap<>();
                telemetry.put("journeyCode", journey.getJourneyCode());
                telemetry.put("passengerName", journey.getPassengerName());
                telemetry.put("passengerPhone", journey.getPassengerPhone());
                telemetry.put("emergencyContactPhone", journey.getEmergencyContactPhone());
                telemetry.put("vesselName", journey.getVesselName());
                telemetry.put("originGhatName", journey.getOriginGhatName());
                telemetry.put("destGhatName", journey.getDestGhatName());
                telemetry.put("status", journey.getStatus());
                telemetry.put("latitude", journey.getCurrentLatitude());
                telemetry.put("longitude", journey.getCurrentLongitude());
                telemetry.put("speedKmh", speedKmh);
                telemetry.put("totalDistanceKm", totalDistanceKm);
                telemetry.put("nearestGhatName", nearestGhat);
                telemetry.put("lastUpdatedAt", journey.getLastUpdatedAt());

                rtdbRef.child(journey.getJourneyCode()).setValue(telemetry);
            }
        } catch (Exception e) {
            Log.d(TAG, "Offline mid-river: Telemetry saved to local Room queue: " + e.getMessage());
        }

        // Mirror to Firestore for long-term audit logs
        try {
            if (firestore != null) {
                firestore.collection(FIRESTORE_JOURNEYS_COLLECTION)
                        .document(journey.getJourneyCode())
                        .set(journey);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Attaches a live listener for family members tracking a specific 6-character voyage code.
     */
    public ValueEventListener startTrackingJourney(@NonNull String journeyCode,
                                                   @NonNull JourneyTrackingCallback callback) {
        final String codeUpper = journeyCode.trim().toUpperCase();

        // 1. Immediately check local Room SQLite cache for instant response
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            ActiveJourneyEntity localJourney = journeyDao.getJourneyByCode(codeUpper);
            if (localJourney != null) {
                mainHandler.post(() -> callback.onJourneyUpdated(localJourney));
            }
        });

        // 2. Attach Firebase Realtime Database listener
        if (rtdbRef != null) {
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            String code = snapshot.child("journeyCode").getValue(String.class);
                            String passName = snapshot.child("passengerName").getValue(String.class);
                            String vessel = snapshot.child("vesselName").getValue(String.class);
                            String origin = snapshot.child("originGhatName").getValue(String.class);
                            String dest = snapshot.child("destGhatName").getValue(String.class);
                            String status = snapshot.child("status").getValue(String.class);
                            Double lat = snapshot.child("latitude").getValue(Double.class);
                            Double lon = snapshot.child("longitude").getValue(Double.class);
                            Double speed = snapshot.child("speedKmh").getValue(Double.class);
                            Double dist = snapshot.child("totalDistanceKm").getValue(Double.class);
                            String near = snapshot.child("nearestGhatName").getValue(String.class);
                            Long updated = snapshot.child("lastUpdatedAt").getValue(Long.class);

                            ActiveJourneyEntity entity = new ActiveJourneyEntity(
                                    code != null ? code : codeUpper,
                                    "",
                                    passName != null ? passName : "Passenger",
                                    "",
                                    "",
                                    vessel != null ? vessel : "River Launch",
                                    "",
                                    origin != null ? origin : "Origin",
                                    "",
                                    dest != null ? dest : "Destination",
                                    0,
                                    updated != null ? updated : System.currentTimeMillis(),
                                    status != null ? status : "ACTIVE",
                                    lat != null ? lat : 23.7045,
                                    lon != null ? lon : 90.4131,
                                    speed != null ? speed : 0.0,
                                    dist != null ? dist : 0.0,
                                    near != null ? near : "En Route",
                                    true
                            );

                            mainHandler.post(() -> callback.onJourneyUpdated(entity));
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing remote journey snapshot: " + e.getMessage());
                        }
                    } else {
                        // Check local Room again before declaring not found
                        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
                            ActiveJourneyEntity fallback = journeyDao.getJourneyByCode(codeUpper);
                            if (fallback != null) {
                                mainHandler.post(() -> callback.onJourneyUpdated(fallback));
                            } else {
                                mainHandler.post(() -> callback.onJourneyNotFound(codeUpper));
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Firebase tracking cancelled: " + error.getMessage());
                    // Fall back to Room
                    ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
                        ActiveJourneyEntity fallback = journeyDao.getJourneyByCode(codeUpper);
                        if (fallback != null) {
                            mainHandler.post(() -> callback.onJourneyUpdated(fallback));
                        } else {
                            mainHandler.post(() -> callback.onError("Network connection weak: " + error.getMessage()));
                        }
                    });
                }
            };

            rtdbRef.child(codeUpper).addValueEventListener(listener);
            return listener;
        } else {
            // Offline/Simulation Mode without Firebase
            ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
                ActiveJourneyEntity fallback = journeyDao.getJourneyByCode(codeUpper);
                if (fallback != null) {
                    mainHandler.post(() -> callback.onJourneyUpdated(fallback));
                } else {
                    mainHandler.post(() -> callback.onJourneyNotFound(codeUpper));
                }
            });
            return null;
        }
    }

    public void stopTrackingJourney(@NonNull String journeyCode, @Nullable ValueEventListener listener) {
        if (rtdbRef != null && listener != null) {
            rtdbRef.child(journeyCode).removeEventListener(listener);
        }
    }

    public void markJourneyEnded(@NonNull String journeyCode) {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            journeyDao.markJourneyStopped(journeyCode);
        });

        try {
            if (rtdbRef != null) {
                rtdbRef.child(journeyCode).child("status").setValue("ARRIVED");
                rtdbRef.child(journeyCode).child("lastUpdatedAt").setValue(System.currentTimeMillis());
            }
        } catch (Exception ignored) {}
    }

    public void fetchBreadcrumbs(@NonNull String journeyCode, @NonNull BreadcrumbCallback callback) {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            List<JourneyBreadcrumbEntity> list = journeyDao.getBreadcrumbsForJourney(journeyCode);
            mainHandler.post(() -> callback.onBreadcrumbsLoaded(list));
        });
    }

    public interface BreadcrumbCallback {
        void onBreadcrumbsLoaded(List<JourneyBreadcrumbEntity> breadcrumbs);
    }
}
