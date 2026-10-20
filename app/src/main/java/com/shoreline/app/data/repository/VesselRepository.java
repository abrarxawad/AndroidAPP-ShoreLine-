package com.shoreline.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.entity.VesselEntity;
import com.shoreline.app.data.remote.FirestoreManager;

import java.util.ArrayList;
import java.util.List;

/**
 * SYLLABUS: Repository Pattern - Offline-First Vessel Data Synchronization
 * Prioritizes Room local database for instant responses mid-river without cellular coverage,
 * while transparently reconciling with Cloud Firestore when online.
 */
public class VesselRepository {

    public interface VesselCallback<T> {
        void onResult(T data);
        void onError(String message);
    }

    private final ShoreLineDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public VesselRepository(Context context) {
        this.db = ShoreLineDatabase.getInstance(context);
        seedDefaultVesselsIfEmpty();
    }

    /**
     * Seeds initial launch fleet into Room for 100% offline demonstration.
     */
    public void seedDefaultVesselsIfEmpty() {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            List<VesselEntity> existing = db.vesselDao().getAllVessels();
            if (existing == null || existing.isEmpty()) {
                List<VesselEntity> seedList = new ArrayList<>();
                seedList.add(new VesselEntity(
                        "vessel_01", "M-01-2489", "MV Parabat-12",
                        "Dhaka (Sadarghat) - Barisal", 950, "2027-12-31",
                        4.6f, 14, false, System.currentTimeMillis()
                ));
                seedList.add(new VesselEntity(
                        "vessel_02", "M-01-3104", "MV Sundarban-10",
                        "Dhaka - Barisal - Patuakhali", 1200, "2027-08-15",
                        4.8f, 22, false, System.currentTimeMillis()
                ));
                seedList.add(new VesselEntity(
                        "vessel_03", "M-01-2850", "MV Manami",
                        "Dhaka (Sadarghat) - Barisal", 850, "2028-01-20",
                        4.9f, 8, false, System.currentTimeMillis()
                ));
                seedList.add(new VesselEntity(
                        "vessel_04", "M-01-1980", "MV Kirtankhola-10",
                        "Dhaka - Barisal", 800, "2026-11-01",
                        4.2f, 35, false, System.currentTimeMillis()
                ));
                seedList.add(new VesselEntity(
                        "vessel_05", "M-02-4512", "MV Green Line-3",
                        "Dhaka - Chandpur (High Speed)", 450, "2027-05-10",
                        4.7f, 5, false, System.currentTimeMillis()
                ));
                seedList.add(new VesselEntity(
                        "vessel_06", "M-01-1205", "MV Pubali-7",
                        "Dhaka - Bhola (Ilisha Ghat)", 750, "2024-03-12", // Expired fitness
                        3.1f, 62, true, System.currentTimeMillis() // Flagged for chronic overload
                ));
                seedList.add(new VesselEntity(
                        "vessel_07", "M-01-3890", "MV Karnaphuli-14",
                        "Dhaka - Bhola - Char Fasson", 1100, "2027-09-30",
                        4.4f, 19, false, System.currentTimeMillis()
                ));

                db.vesselDao().insertAll(seedList);
            }
        });
    }

    /**
     * SYLLABUS: Offline-First Lookup (Room First -> Firestore Parallel Reconcile)
     */
    public void getVesselByNumber(String queryText, VesselCallback<VesselEntity> callback) {
        final String sanitized = queryText != null ? queryText.trim().toUpperCase() : "";

        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            // Step 1: Check local Room cache
            VesselEntity cachedVessel = db.vesselDao().getVesselByRegistrationNumber(sanitized);
            if (cachedVessel != null) {
                mainHandler.post(() -> callback.onResult(cachedVessel));
            }

            // Step 2: Parallel reconcile with Cloud Firestore
            try {
                FirestoreManager.getInstance().getVesselsCollection()
                        .whereEqualTo("registrationNumber", sanitized)
                        .limit(1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                VesselEntity remoteVessel = mapFirestoreDocToVessel(doc);
                                // Update Room local cache
                                ShoreLineDatabase.databaseWriteExecutor.execute(() ->
                                        db.vesselDao().insertOrUpdateVessel(remoteVessel));

                                if (cachedVessel == null) {
                                    mainHandler.post(() -> callback.onResult(remoteVessel));
                                }
                            } else if (cachedVessel == null) {
                                mainHandler.post(() -> callback.onError("No vessel found matching: " + sanitized));
                            }
                        });
            } catch (Exception e) {
                if (cachedVessel == null) {
                    mainHandler.post(() -> callback.onError("Offline: Vessel not found in local cache"));
                }
            }
        });
    }

    public void getAllVessels(VesselCallback<List<VesselEntity>> callback) {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            List<VesselEntity> localVessels = db.vesselDao().getAllVessels();
            mainHandler.post(() -> callback.onResult(localVessels != null ? localVessels : new ArrayList<>()));

            // Sync with Firestore in background if online
            try {
                FirestoreManager.getInstance().getVesselsCollection()
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                                List<VesselEntity> remoteList = new ArrayList<>();
                                for (DocumentSnapshot doc : task.getResult()) {
                                    remoteList.add(mapFirestoreDocToVessel(doc));
                                }
                                ShoreLineDatabase.databaseWriteExecutor.execute(() ->
                                        db.vesselDao().insertAll(remoteList));
                            }
                        });
            } catch (Exception ignored) {}
        });
    }

    private VesselEntity mapFirestoreDocToVessel(DocumentSnapshot doc) {
        Long cap = doc.getLong("licensedCapacity");
        Long reps = doc.getLong("totalReports");
        Double rating = doc.getDouble("communityRating");
        Boolean flagged = doc.getBoolean("isFlagged");

        return new VesselEntity(
                doc.getId(),
                doc.getString("registrationNumber"),
                doc.getString("vesselName"),
                doc.getString("routeName"),
                cap != null ? cap.intValue() : 500,
                doc.getString("fitnessExpiryDate"),
                rating != null ? rating.floatValue() : 4.0f,
                reps != null ? reps.intValue() : 0,
                flagged != null && flagged,
                System.currentTimeMillis()
        );
    }
}
