package com.shoreline.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.Locale;

/**
 * SYLLABUS: Implicit Intents Architecture (ACTION_DIAL, ACTION_SENDTO, ACTION_VIEW, ACTION_SEND)
 *
 * Demonstrates Android Intent filters and package resolution without hardcoded target components:
 * 1. ACTION_DIAL: Launches system phone dialer with emergency rescue authority numbers.
 * 2. ACTION_SENDTO: Dispatches pre-composed emergency SMS with GPS river coordinates.
 * 3. ACTION_VIEW: Opens geographic coordinates in navigation apps using standard 'geo:' URI scheme.
 * 4. ACTION_SEND: Invokes the native Android share sheet for instant multi-app broadcast.
 */
public final class IntentHelper {

    public static final String BIWTA_RESCUE_HOTLINE = "16107";
    public static final String NATIONAL_EMERGENCY_999 = "999";

    private IntentHelper() {}

    /**
     * SYLLABUS: Implicit Intent - ACTION_DIAL
     * Opens dialer with number pre-filled; does not require dangerous CALL_PHONE runtime permission.
     */
    public static void dialEmergencyNumber(Context context, String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Dialer unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * SYLLABUS: Implicit Intent - ACTION_SENDTO with 'smsto:' Scheme
     * Pre-fills emergency SMS recipient and message body.
     */
    public static void sendEmergencySms(Context context, String phoneNumber, String messageBody) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", messageBody);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "SMS client unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * SYLLABUS: Implicit Intent - ACTION_VIEW with 'geo:' Scheme
     * Displays river incident coordinates on external mapping applications.
     */
    public static void openGeoLocation(Context context, double latitude, double longitude, String label) {
        try {
            String uriString = String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)",
                    latitude, longitude, latitude, longitude, Uri.encode(label));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Map application unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * SYLLABUS: Implicit Intent - ACTION_SEND with Chooser
     * Broadcasts distress message across any messaging or social app installed on the device.
     */
    public static void shareDistressMessage(Context context, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "EMERGENCY: River Vessel Distress SOS");
            intent.putExtra(Intent.EXTRA_TEXT, message);

            Intent chooser = Intent.createChooser(intent, "Share Emergency SOS Broadcast");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(context, "Share sheet unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
