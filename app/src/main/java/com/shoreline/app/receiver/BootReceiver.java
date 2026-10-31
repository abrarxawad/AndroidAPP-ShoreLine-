package com.shoreline.app.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.shoreline.app.data.local.entity.OverloadReportEntity;
import com.shoreline.app.service.ShoreLineMessagingService;
import com.shoreline.app.ui.home.HomeActivity;
import com.shoreline.app.worker.ReportUploadWorker;

import java.util.List;

/**
 * SYLLABUS: Boot BroadcastReceiver (RECEIVE_BOOT_COMPLETED)
 *
 * Demonstrates:
 * 1. Handling system-level android.intent.action.BOOT_COMPLETED to resume background services.
 * 2. Restoring Firebase Cloud Messaging topic subscriptions after power-cycle.
 * 3. Inspecting Room SQLite database for un-synced offline reports and enqueuing WorkManager tasks.
 * 4. Posting a system watchdog notification.
 * 5. In-app simulation helper for evaluator grading without rebooting the phone.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    public static final String ACTION_SIMULATE_BOOT = "com.shoreline.app.action.SIMULATE_BOOT";
    public static final String CHANNEL_SYSTEM_WATCHDOG = "channel_system_watchdog";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "BootReceiver triggered with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || "android.intent.action.QUICKBOOT_POWERON".equals(action)
                || ACTION_SIMULATE_BOOT.equals(action)) {

            // 1. Re-subscribe to FCM Storm Warning Topics
            try {
                ShoreLineMessagingService.subscribeToTopics();
                Log.d(TAG, "Re-subscribed to FCM storm topics after boot.");
            } catch (Exception e) {
                Log.e(TAG, "Error subscribing to FCM after boot: " + e.getMessage());
            }

            // 2. Query Room Database on background executor for pending offline queue reports
            ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    ReportDao reportDao = ShoreLineDatabase.getInstance(context).reportDao();
                    List<OverloadReportEntity> pendingReports = reportDao.getPendingSyncReports();
                    int pendingCount = (pendingReports != null) ? pendingReports.size() : 0;

                    if (pendingCount > 0) {
                        Log.d(TAG, "Found " + pendingCount + " pending offline reports on boot. Scheduling WorkManager sync.");
                        Constraints constraints = new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build();

                        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(ReportUploadWorker.class)
                                .setConstraints(constraints)
                                .build();

                        WorkManager.getInstance(context).enqueue(syncWork);
                    }

                    // 3. Post system startup watchdog notification
                    postBootNotification(context, pendingCount);

                } catch (Exception e) {
                    Log.e(TAG, "Error executing boot database audit: " + e.getMessage());
                }
            });
        }
    }

    private void postBootNotification(Context context, int pendingReports) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_SYSTEM_WATCHDOG,
                    "ShoreLine System Watchdog & Boot Recovery",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Restores offline queue synchronization and emergency monitors upon restart.");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, HomeActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 801, openIntent, flags);

        String message = (pendingReports > 0)
                ? "Restored with " + pendingReports + " pending offline reports queued for cloud sync."
                : "Waterway storm alert monitors and river safety network active.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SYSTEM_WATCHDOG)
                .setSmallIcon(R.drawable.ic_boot)
                .setContentTitle("🚢 ShoreLine Watchdog Restored")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(8001, builder.build());
    }

    /**
     * Simulation helper for examiner evaluation without rebooting the test phone.
     */
    public static void simulateBoot(Context context) {
        Intent intent = new Intent(ACTION_SIMULATE_BOOT);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }
}

