package com.shoreline.app.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shoreline.app.R;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.dao.ReportDao;
import com.shoreline.app.data.local.entity.OverloadReportEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * SYLLABUS: WorkManager - Guaranteed background offline queue sync with network constraints.
 * Automatically wakes up when device regains network connectivity (cellular/Wi-Fi),
 * drains the local Room offline queue, uploads evidence to Firestore, and marks records as synced.
 */
public class ReportUploadWorker extends Worker {

    private static final String TAG = "ReportUploadWorker";
    private static final String CHANNEL_ID = "channel_report_sync";

    public ReportUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting background sync of pending overload reports...");

        Context context = getApplicationContext();
        ShoreLineDatabase database = ShoreLineDatabase.getInstance(context);
        ReportDao reportDao = database.reportDao();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        List<OverloadReportEntity> pendingReports = reportDao.getPendingSyncReports();
        if (pendingReports == null || pendingReports.isEmpty()) {
            Log.d(TAG, "No pending reports found in Room offline queue.");
            return Result.success();
        }

        Log.d(TAG, "Found " + pendingReports.size() + " pending reports to upload.");
        int syncedCount = 0;

        for (OverloadReportEntity report : pendingReports) {
            try {
                Map<String, Object> reportData = new HashMap<>();
                reportData.put("reportId", report.getReportId());
                reportData.put("vesselId", report.getVesselId());
                reportData.put("vesselNumber", report.getVesselNumber());
                reportData.put("crowdLevel", report.getCrowdLevel());
                reportData.put("estimatedPassengerCount", report.getEstimatedPassengerCount());
                reportData.put("latitude", report.getLatitude());
                reportData.put("longitude", report.getLongitude());
                reportData.put("timestamp", report.getTimestamp());
                reportData.put("photoBase64", report.getPhotoBase64());
                reportData.put("reporterUserId", report.getReporterUserId());
                reportData.put("isAnonymous", report.isAnonymous());
                reportData.put("status", "SUBMITTED");

                // Blocking upload with 15 second timeout per report
                Tasks.await(
                        firestore.collection("reports").document(report.getReportId()).set(reportData),
                        15,
                        TimeUnit.SECONDS
                );

                // Update Room status to SYNCED
                reportDao.markAsSynced(report.getReportId(), System.currentTimeMillis());
                syncedCount++;
                Log.d(TAG, "Successfully synced report: " + report.getReportId());

            } catch (Exception e) {
                Log.e(TAG, "Failed to upload report: " + report.getReportId(), e);
                // Return retry so WorkManager applies exponential backoff
                return Result.retry();
            }
        }

        // Post completion notification to alert user that river report reached port authorities
        showSyncCompleteNotification(context, syncedCount);

        return Result.success();
    }

    private void showSyncCompleteNotification(Context context, int count) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Offline Report Sync",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Alerts when offline waterway safety reports are synced with Port Authority");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_shoreline_logo)
                .setContentTitle("ShoreLine Sync Complete")
                .setContentText(count + " offline overload report(s) uploaded to BIWTA Port Authority.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
    }
}
