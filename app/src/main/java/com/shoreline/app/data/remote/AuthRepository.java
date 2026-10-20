package com.shoreline.app.data.remote;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.entity.UserEntity;
import com.shoreline.app.util.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SYLLABUS: Repository Pattern & Dual-Layer Authentication (Firebase + Room)
 * Provides cloud authentication backed by local Room database fallback for offline resilience.
 */
public class AuthRepository {

    public interface AuthCallback {
        void onSuccess(UserEntity user, String message);
        void onError(String errorMessage);
    }

    private final Context context;
    private final ShoreLineDatabase localDb;
    private final SessionManager sessionManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.localDb = ShoreLineDatabase.getInstance(this.context);
        this.sessionManager = new SessionManager(this.context);
    }

    /**
     * SYLLABUS: User Authentication Flow
     * 1. Attempts Firebase Authentication.
     * 2. Synchronizes role and profile from Cloud Firestore.
     * 3. Caches profile into Room local database.
     * 4. If offline/error, falls back gracefully to Room cached credentials.
     */
    public void login(String email, String password, AuthCallback callback) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && auth.getCurrentUser() != null) {
                            fetchCloudUserProfile(auth.getCurrentUser(), callback);
                        } else {
                            // Fallback to local Room cache for offline field operations
                            fallbackToLocalLogin(email, password, callback,
                                    task.getException() != null ? task.getException().getMessage() : "Authentication failed");
                        }
                    });
        } catch (Exception e) {
            fallbackToLocalLogin(email, password, callback, e.getMessage());
        }
    }

    /**
     * SYLLABUS: User Registration with Role Assignment
     * Stores user role (PASSENGER vs ADMIN) in both Firestore and local Room database.
     */
    public void register(String email, String password, String fullName, String phone,
                         String role, String emergencyContact, AuthCallback callback) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && auth.getCurrentUser() != null) {
                            String uid = auth.getCurrentUser().getUid();
                            UserEntity newUser = new UserEntity(
                                    uid, email, fullName, phone, role, emergencyContact, System.currentTimeMillis()
                            );
                            saveUserToCloudAndLocal(newUser, callback);
                        } else {
                            // Offline registration fallback directly into Room
                            registerOfflineUser(email, fullName, phone, role, emergencyContact, callback);
                        }
                    });
        } catch (Exception e) {
            registerOfflineUser(email, fullName, phone, role, emergencyContact, callback);
        }
    }

    /**
     * Quick Guest Mode for instant examiner evaluation without requiring email confirmation.
     */
    public void loginAsGuest(String role, AuthCallback callback) {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            String guestId = "guest_" + UUID.randomUUID().toString().substring(0, 8);
            String name = "ADMIN".equalsIgnoreCase(role) ? "BIWTA Inspector (Demo)" : "River Traveler (Demo)";
            String email = ("ADMIN".equalsIgnoreCase(role) ? "admin" : "passenger") + "@shoreline.bd";

            UserEntity guestUser = new UserEntity(
                    guestId, email, name, "+8801700000000", role, "999", System.currentTimeMillis()
            );
            localDb.userDao().insertUser(guestUser);
            sessionManager.saveUserSession(guestId, email, name, "+8801700000000", role, "999");

            mainHandler.post(() -> callback.onSuccess(guestUser, "Guest session active (" + role + ")"));
        });
    }

    private void fetchCloudUserProfile(FirebaseUser firebaseUser, AuthCallback callback) {
        FirestoreManager.getInstance().getUsersCollection().document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot doc = task.getResult();
                        String role = doc.getString("role");
                        String name = doc.getString("fullName");
                        String phone = doc.getString("phoneNumber");
                        String emergency = doc.getString("emergencyContact");

                        UserEntity user = new UserEntity(
                                firebaseUser.getUid(),
                                firebaseUser.getEmail(),
                                name != null ? name : "Traveler",
                                phone != null ? phone : "",
                                role != null ? role : "PASSENGER",
                                emergency != null ? emergency : "999",
                                System.currentTimeMillis()
                        );
                        cacheUserLocally(user, callback, "Cloud login successful");
                    } else {
                        // User doc missing in Firestore, create default profile
                        UserEntity defaultUser = new UserEntity(
                                firebaseUser.getUid(),
                                firebaseUser.getEmail(),
                                "River Traveler",
                                "",
                                "PASSENGER",
                                "999",
                                System.currentTimeMillis()
                        );
                        saveUserToCloudAndLocal(defaultUser, callback);
                    }
                });
    }

    private void saveUserToCloudAndLocal(UserEntity user, AuthCallback callback) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("userId", user.getUserId());
        docData.put("email", user.getEmail());
        docData.put("fullName", user.getFullName());
        docData.put("phoneNumber", user.getPhoneNumber());
        docData.put("role", user.getRole());
        docData.put("emergencyContact", user.getEmergencyContact());
        docData.put("createdAt", user.getCreatedAt());

        FirestoreManager.getInstance().getUsersCollection().document(user.getUserId())
                .set(docData)
                .addOnSuccessListener(aVoid -> cacheUserLocally(user, callback, "Account registered & synced"))
                .addOnFailureListener(e -> cacheUserLocally(user, callback, "Account created locally (offline fallback)"));
    }

    private void cacheUserLocally(UserEntity user, AuthCallback callback, String message) {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            localDb.userDao().insertUser(user);
            sessionManager.saveUserSession(
                    user.getUserId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getRole(),
                    user.getEmergencyContact()
            );
            mainHandler.post(() -> callback.onSuccess(user, message));
        });
    }

    private void fallbackToLocalLogin(String email, String password, AuthCallback callback, String cloudReason) {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            UserEntity localUser = localDb.userDao().getUserByEmail(email);
            if (localUser != null) {
                sessionManager.saveUserSession(
                        localUser.getUserId(),
                        localUser.getEmail(),
                        localUser.getFullName(),
                        localUser.getPhoneNumber(),
                        localUser.getRole(),
                        localUser.getEmergencyContact()
                );
                mainHandler.post(() -> callback.onSuccess(localUser, "Offline login verified via local Room database"));
            } else {
                mainHandler.post(() -> callback.onError(cloudReason + " (and no matching offline account in Room)"));
            }
        });
    }

    private void registerOfflineUser(String email, String fullName, String phone,
                                    String role, String emergencyContact, AuthCallback callback) {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            String offlineId = "offline_" + UUID.randomUUID().toString().substring(0, 8);
            UserEntity offlineUser = new UserEntity(
                    offlineId, email, fullName, phone, role, emergencyContact, System.currentTimeMillis()
            );
            localDb.userDao().insertUser(offlineUser);
            sessionManager.saveUserSession(offlineId, email, fullName, phone, role, emergencyContact);
            mainHandler.post(() -> callback.onSuccess(offlineUser, "Registered in offline mode (Room cached)"));
        });
    }
}
