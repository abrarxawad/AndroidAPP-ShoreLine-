package com.shoreline.app.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.shoreline.app.R;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.dao.JourneyDao;
import com.shoreline.app.data.local.entity.ActiveJourneyEntity;
import com.shoreline.app.ui.tracker.FamilyTrackerActivity;

/**
 * SYLLABUS: Battery BroadcastReceiver (ACTION_BATTERY_LOW)
 *
 * Demonstrates:
 * 1. Handling system-level android.intent.action.BATTERY_LOW during riverway transit.
 * 2. Cross-referencing Room SQLite to detect if user is currently on an active voyage.
 * 3. Issuing urgent advisory notification to preserve battery life and cache offline confluences.
 * 4. Local simulation helper for viva / lab evaluation.
 */
public class BatteryStateReceiver extends BroadcastReceiver {

    private static final String TAG = "BatteryStateReceiver";
    public static final String ACTION_SIMULATE_BATTERY_LOW = "com.shoreline.app.action.SIMULATE_BATTERY_LOW";
    public static final String ACTION_BATTERY_WARNING_DISPLAY = "com.shoreline.app.action.BATTERY_WARNING_DISPLAY";
    public static final String CHANNEL_BATTERY_ALERTS = "channel_battery_alerts";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "BatteryStateReceiver triggered with action: " + action);

        boolean isLowBattery = Intent.ACTION_BATTERY_LOW.equals(action)
                || ACTION_SIMULATE_BATTERY_LOW.equals(action);

        if (isLowBattery) {
            // Check if user has an active voyage running in Room
            ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    JourneyDao journeyDao = ShoreLineDatabase.getInstance(context).journeyDao();
                    ActiveJourneyEntity activeJourney = journeyDao.getActiveJourney();
                    boolean hasActiveJourney = (activeJourney != null);

                    postLowBatteryNotification(context, hasActiveJourney);

                    // Broadcast in-app warning so open screens can display an emergency alert banner
                    Intent inAppIntent = new Intent(ACTION_BATTERY_WARNING_DISPLAY);
                    inAppIntent.setPackage(context.getPackageName());
                    inAppIntent.putExtra("EXTRA_HAS_VOYAGE", hasActiveJourney);
                    context.sendBroadcast(inAppIntent);

                } catch (Exception e) {
                    Log.e(TAG, "Error checking active journey for battery warning: " + e.getMessage());
                }
            });
        }
    }

    private void postLowBatteryNotification(Context context, boolean hasActiveVoyage) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_BATTERY_ALERTS,
                    "Waterway Voyage Battery Warnings",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Critical alerts when phone battery drops during river transit.");
            channel.enableLights(true);
            channel.setLightColor(Color.YELLOW);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, FamilyTrackerActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 802, openIntent, flags);

        String title = hasActiveVoyage
                ? "⚠️ CRITICAL BATTERY: Active River Voyage!"
                : "⚠️ Waterway Advisory: Low Battery (<15%)";

        String message = hasActiveVoyage
                ? "Phone battery is below 15% while en route. Turn on Power Saver, share your tracking code, and note the nearest ghat before shutdown."
                : "Phone battery is below 15%. If travelling by river launch, ensure emergency contacts and offline maps are cached.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_BATTERY_ALERTS)
                .setSmallIcon(R.drawable.ic_battery_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.parseColor("#F57F17"))
                .setVibrate(new long[]{0, 300, 150, 300});

        manager.notify(8002, builder.build());
    }

    /**
     * Simulation helper for examiner evaluation.
     */
    public static void simulateLowBattery(Context context) {
        Intent intent = new Intent(ACTION_SIMULATE_BATTERY_LOW);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }
}

