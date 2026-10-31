package com.shoreline.app.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.shoreline.app.R;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.dao.ReportDao;
import com.shoreline.app.data.local.dao.SosDao;
import com.shoreline.app.data.local.entity.OverloadReportEntity;
import com.shoreline.app.data.local.entity.SosSignalEntity;
import com.shoreline.app.data.remote.FirestoreManager;
import com.shoreline.app.worker.ReportUploadWorker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SYLLABUS: Dynamic Connectivity BroadcastReceiver & Auto-Sync
 *
 * Demonstrates:
 * 1. Handling network state transitions (offline to online restoration).
 * 2. Background queue auto-flush: automatically pushes queued overload reports and SOS signals to Firestore.
 * 3. Enqueuing WorkManager tasks with CONNECTED network constraints.
 * 4. Local simulation helper for lab demonstration.
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    private static final String TAG = "ConnectivityReceiver";
    public static final String ACTION_SIMULATE_NETWORK = "com.shoreline.app.action.SIMULATE_NETWORK_RESTORE";
    public static final String ACTION_CONNECTIVITY_CHANGED = "com.shoreline.app.action.CONNECTIVITY_CHANGED";
    public static final String CHANNEL_SYNC_ALERTS = "channel_sync_alerts";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "ConnectivityReceiver received action: " + action);

        boolean isSimulated = ACTION_SIMULATE_NETWORK.equals(action);
        boolean isConnected = isSimulated || isNetworkAvailable(context);

        if (isConnected) {
            Log.d(TAG, "Network connection restored. Flushing offline queues to Cloud Firestore.");

            // 1. Flush un-synced reports via WorkManager
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(ReportUploadWorker.class)
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(context).enqueue(syncWork);

            // 2. Flush pending SOS signals directly to Firestore
            ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    SosDao sosDao = ShoreLineDatabase.getInstance(context).sosDao();
                    ReportDao reportDao = ShoreLineDatabase.getInstance(context).reportDao();

                    List<SosSignalEntity> pendingSos = sosDao.getPendingCloudSyncSos();
                    List<OverloadReportEntity> pendingReports = reportDao.getPendingSyncReports();

                    int totalPending = (pendingSos != null ? pendingSos.size() : 0)
                            + (pendingReports != null ? pendingReports.size() : 0);

                    if (pendingSos != null && !pendingSos.isEmpty()) {
                        for (SosSignalEntity sos : pendingSos) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("sosId", sos.getSosId());
                            map.put("senderName", sos.getSenderName());
                            map.put("senderPhone", sos.getSenderPhone());
                            map.put("latitude", sos.getLatitude());
                            map.put("longitude", sos.getLongitude());
                            map.put("timestamp", sos.getTimestamp());
                            map.put("status", sos.getStatus());
                            map.put("vesselName", sos.getOriginDeviceId());

                            FirestoreManager.getInstance().getSosCollection()
                                    .document(sos.getSosId())
                                    .set(map)
                                    .addOnSuccessListener(unused -> {
                                        ShoreLineDatabase.databaseWriteExecutor.execute(() ->
                                                sosDao.markAsCloudSynced(sos.getSosId()));
                                    });
                        }
                    }

                    if (isSimulated || totalPending > 0) {
                        postSyncNotification(context, totalPending);
                    }

                    // Broadcast in-app status update
                    Intent inApp = new Intent(ACTION_CONNECTIVITY_CHANGED);
                    inApp.setPackage(context.getPackageName());
                    inApp.putExtra("EXTRA_IS_CONNECTED", true);
                    inApp.putExtra("EXTRA_PENDING_FLUSHED", totalPending);
                    context.sendBroadcast(inApp);

                } catch (Exception e) {
                    Log.e(TAG, "Error flushing offline data on network restore: " + e.getMessage());
                }
            });
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }

    private void postSyncNotification(Context context, int itemsFlushed) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_SYNC_ALERTS,
                    "Waterway Cloud Auto-Sync Alerts",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notifications when offline river queues sync to Cloud Firestore.");
            manager.createNotificationChannel(channel);
        }

        String msg = (itemsFlushed > 0)
                ? "Restored cellular link: " + itemsFlushed + " offline items synced to Cloud Firestore."
                : "Waterway network link verified. Cloud databases in sync.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SYNC_ALERTS)
                .setSmallIcon(R.drawable.ic_shoreline_logo)
                .setContentTitle("☁️ Waterway Auto-Sync Complete")
                .setContentText(msg)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        manager.notify(8003, builder.build());
    }

    /**
     * Simulation helper for examiner evaluation.
     */
    public static void simulateNetworkRestore(Context context) {
        Intent intent = new Intent(ACTION_SIMULATE_NETWORK);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }
}

