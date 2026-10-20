package com.shoreline.app.data.repository;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.dao.ReportDao;
import com.shoreline.app.data.local.entity.OverloadReportEntity;
import com.shoreline.app.worker.ReportUploadWorker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SYLLABUS: Repository Pattern & Offline-First Data Synchronization
 * Manages saving overload reports into Room DB and scheduling guaranteed upload via WorkManager.
 */
public class ReportRepository {

    public static final String WORK_NAME_REPORT_SYNC = "shoreline_report_upload_work";

    public interface ReportCallback<T> {
        void onResult(T data);
        void onError(String message);
    }

    private final Context context;
    private final ReportDao reportDao;
    private final WorkManager workManager;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public ReportRepository(Context context) {
        this.context = context.getApplicationContext();
        ShoreLineDatabase database = ShoreLineDatabase.getInstance(context);
        this.reportDao = database.reportDao();
        this.workManager = WorkManager.getInstance(context);
        this.firestore = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    /**
     * SYLLABUS: Offline Queueing Flow
     * 1. Save into Room DB immediately (ensures 100% data safety even on total power loss).
     * 2. Schedule guaranteed background sync with WorkManager (NetworkType.CONNECTED constraint).
     * 3. If connected right now, attempt immediate Firestore upload and mark synced.
     */
    public void submitReport(OverloadReportEntity report, ReportCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                // Step 1: Always persist into Room DB first
                reportDao.insertReport(report);

                // Step 2: Schedule WorkManager constraint-based background upload
                enqueueBackgroundSync();

                // Step 3: If already online, push immediately
                if (isNetworkAvailable()) {
                    tryUploadDirectly(report, callback);
                } else {
                    mainHandler.post(() -> callback.onResult(false)); // false = queued offline
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Database error: " + e.getMessage()));
            }
        });
    }

    private void tryUploadDirectly(OverloadReportEntity report, ReportCallback<Boolean> callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("reportId", report.getReportId());
        data.put("vesselId", report.getVesselId());
        data.put("vesselNumber", report.getVesselNumber());
        data.put("crowdLevel", report.getCrowdLevel());
        data.put("estimatedPassengerCount", report.getEstimatedPassengerCount());
        data.put("latitude", report.getLatitude());
        data.put("longitude", report.getLongitude());
        data.put("timestamp", report.getTimestamp());
        data.put("photoBase64", report.getPhotoBase64());
        data.put("reporterUserId", report.getReporterUserId());
        data.put("isAnonymous", report.isAnonymous());
        data.put("status", "SUBMITTED");

        firestore.collection("reports").document(report.getReportId()).set(data)
                .addOnSuccessListener(aVoid -> executorService.execute(() -> {
                    reportDao.markAsSynced(report.getReportId(), System.currentTimeMillis());
                    mainHandler.post(() -> callback.onResult(true)); // true = uploaded immediately
                }))
                .addOnFailureListener(e -> {
                    // Direct upload failed (e.g. river spotty connection); WorkManager will handle it
                    mainHandler.post(() -> callback.onResult(false));
                });
    }

    public void enqueueBackgroundSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(ReportUploadWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();

        workManager.enqueueUniqueWork(WORK_NAME_REPORT_SYNC, ExistingWorkPolicy.KEEP, syncRequest);
    }

    public void forceSyncNow() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(ReportUploadWorker.class)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniqueWork(WORK_NAME_REPORT_SYNC, ExistingWorkPolicy.REPLACE, syncRequest);
    }

    public void getAllReports(ReportCallback<List<OverloadReportEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<OverloadReportEntity> list = reportDao.getAllReports();
                mainHandler.post(() -> callback.onResult(list));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Failed to load reports: " + e.getMessage()));
            }
        });
    }

    public void getPendingCount(ReportCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                List<OverloadReportEntity> pending = reportDao.getPendingSyncReports();
                int count = pending != null ? pending.size() : 0;
                mainHandler.post(() -> callback.onResult(count));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
}
