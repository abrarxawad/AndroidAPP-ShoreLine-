package com.shoreline.app.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.shoreline.app.R;
import com.shoreline.app.ui.sos.OfflineSosActivity;
import com.shoreline.app.util.IntentHelper;

import java.util.Locale;

/**
 * SYLLABUS: Custom BroadcastReceiver - In-App Emergency SOS Alarm
 *
 * Receives package-restricted implicit/explicit broadcasts when a distress beacon
 * is picked up over the Nearby Connections mesh relay.
 * Emits SOS Morse code haptic vibration (... --- ...) and posts a high-priority heads-up notification.
 */
public class SosAlertReceiver extends BroadcastReceiver {

    private static final String TAG = "SosAlertReceiver";
    public static final String ACTION_SOS_BEACON_DETECTED = "com.shoreline.app.action.SOS_BEACON_DETECTED";
    public static final String CHANNEL_ID_SOS = "shoreline_emergency_sos_channel";
    public static final int NOTIFICATION_ID_SOS = 9110;

    public static final String EXTRA_SOS_ID = "extra_sos_id";
    public static final String EXTRA_SENDER_NAME = "extra_sender_name";
    public static final String EXTRA_VESSEL_NAME = "extra_vessel_name";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_EMERGENCY_TYPE = "extra_emergency_type";
    public static final String EXTRA_HOP_COUNT = "extra_hop_count";

    // International Morse Code for SOS (... --- ...)
    private static final long[] SOS_VIBRATION_PATTERN = {
            0,
            200, 100, 200, 100, 200, // . . . (S)
            300,
            500, 100, 500, 100, 500, // - - - (O)
            300,
            200, 100, 200, 100, 200  // . . . (S)
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_SOS_BEACON_DETECTED.equals(intent.getAction())) return;

        String sosId = intent.getStringExtra(EXTRA_SOS_ID);
        String senderName = intent.getStringExtra(EXTRA_SENDER_NAME);
        String vesselName = intent.getStringExtra(EXTRA_VESSEL_NAME);
        double lat = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0);
        double lon = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0);
        String type = intent.getStringExtra(EXTRA_EMERGENCY_TYPE);
        int hopCount = intent.getIntExtra(EXTRA_HOP_COUNT, 0);

        Log.w(TAG, String.format(Locale.US, "🚨 SOS ALERT RECEIVED! Vessel: %s, Type: %s, Hop: %d", vesselName, type, hopCount));

        // 1. Trigger SOS Morse Code Haptic Vibration
        triggerSosVibration(context);

        // 2. Post High-Priority Heads-Up Alert Notification
        showEmergencyNotification(context, sosId, senderName, vesselName, lat, lon, type, hopCount);
    }

    private void triggerSosVibration(Context context) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(SOS_VIBRATION_PATTERN, -1));
                } else {
                    vibrator.vibrate(SOS_VIBRATION_PATTERN, -1);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Vibration failed: " + e.getMessage());
        }
    }

    private void showEmergencyNotification(Context context, String sosId, String sender,
                                           String vessel, double lat, double lon,
                                           String type, int hopCount) {

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        createNotificationChannel(manager);

        // Intent to open OfflineSosActivity
        Intent openIntent = new Intent(context, OfflineSosActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openIntent.putExtra(EXTRA_SOS_ID, sosId);
        PendingIntent pendingOpen = PendingIntent.getActivity(
                context, 101, openIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Intent to call BIWTA Rescue hotline (16107)
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + IntentHelper.BIWTA_RESCUE_HOTLINE));
        PendingIntent pendingDial = PendingIntent.getActivity(
                context, 102, dialIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String title = String.format("🚨 MAYDAY / DISTRESS BEACON: %s", vessel != null ? vessel : "Nearby Vessel");
        String content = String.format(Locale.US, "Emergency: %s | Relay Hop: %d | Location: %.4f, %.4f",
                type != null ? type : "Vessel in distress", hopCount, lat, lon);

        Uri alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_SOS)
                .setSmallIcon(R.drawable.ic_shoreline_logo)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        String.format(Locale.US, "%s\nSender: %s\nCoordinates: %.4f° N, %.4f° E\nReceived over Nearby P2P Mesh.", content, sender, lat, lon)))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(alertSound)
                .setVibrate(SOS_VIBRATION_PATTERN)
                .setAutoCancel(true)
                .setContentIntent(pendingOpen)
                .addAction(android.R.drawable.ic_menu_search, "View Radar", pendingOpen)
                .addAction(android.R.drawable.ic_menu_call, "Call 16107", pendingDial);

        manager.notify(NOTIFICATION_ID_SOS, builder.build());
    }

    private void createNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_SOS,
                    "Emergency River SOS Beacons",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Critical alerts when a nearby boat sends a distress beacon");
            channel.enableVibration(true);
            channel.setVibrationPattern(SOS_VIBRATION_PATTERN);
            manager.createNotificationChannel(channel);
        }
    }
}
