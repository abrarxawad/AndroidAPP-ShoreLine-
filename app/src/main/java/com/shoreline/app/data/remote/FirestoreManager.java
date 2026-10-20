package com.shoreline.app.data.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * SYLLABUS: Cloud Firestore Integration with Offline Persistence
 * Provides schema endpoints and configures device-level caching for river journeys.
 */
public class FirestoreManager {

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_VESSELS = "vessels";
    public static final String COLLECTION_REPORTS = "reports";
    public static final String COLLECTION_SOS = "sos_signals";
    public static final String COLLECTION_ALERTS = "storm_alerts";

    private static volatile FirestoreManager INSTANCE;
    private final FirebaseFirestore firestore;

    private FirestoreManager() {
        firestore = FirebaseFirestore.getInstance();
        // SYLLABUS: Enable Firestore Offline Persistence on free Spark plan
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        firestore.setFirestoreSettings(settings);
    }

    public static synchronized FirestoreManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FirestoreManager();
        }
        return INSTANCE;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public CollectionReference getUsersCollection() {
        return firestore.collection(COLLECTION_USERS);
    }

    public CollectionReference getVesselsCollection() {
        return firestore.collection(COLLECTION_VESSELS);
    }

    public CollectionReference getReportsCollection() {
        return firestore.collection(COLLECTION_REPORTS);
    }

    public CollectionReference getSosCollection() {
        return firestore.collection(COLLECTION_SOS);
    }

    public CollectionReference getAlertsCollection() {
        return firestore.collection(COLLECTION_ALERTS);
    }
}
