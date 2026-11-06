package com.shoreline.app.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * Tactile Haptic Feedback Helper
 * Enhances cockpit user experience with subtle physical touch responses.
 */
public class HapticHelper {

    /**
     * Subtle click feedback on button or card tap.
     */
    public static void vibrateClick(View view) {
        if (view == null) return;
        try {
            view.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            );
        } catch (Exception ignored) {}
    }

    /**
     * Light tick feedback on dragging sliders/gauges or toggling chips.
     */
    public static void vibrateTick(View view) {
        if (view == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.performHapticFeedback(
                        HapticFeedbackConstants.CLOCK_TICK,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                );
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Warning double-pulse vibration for safety alerts or critical events.
     */
    public static void vibrateWarning(Context context) {
        if (context == null) return;
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    long[] pattern = {0, 80, 60, 120};
                    int[] amplitudes = {0, 200, 0, 255};
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1));
                } else {
                    vibrator.vibrate(new long[]{0, 80, 60, 120}, -1);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * High-priority Mayday distress vibration sequence.
     */
    public static void vibrateSosEmergency(Context context) {
        if (context == null) return;
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                // S.O.S Morse Code: ... --- ...
                long[] pattern = {0, 150, 100, 150, 100, 150, 250, 400, 150, 400, 150, 400, 250, 150, 100, 150, 100, 150};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                } else {
                    vibrator.vibrate(pattern, -1);
                }
            }
        } catch (Exception ignored) {}
    }
}

