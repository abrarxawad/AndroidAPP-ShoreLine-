package com.shoreline.app.util;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;

/**
 * Maritime Acoustic Mayday Siren & Foghorn Alert System
 * Synthesizes high-decibel audible emergency alarms without requiring external media assets.
 */
public class AcousticSirenHelper {

    private static ToneGenerator toneGenerator;
    private static Handler sirenHandler;
    private static Runnable sirenRunnable;
    private static boolean isAlarmActive = false;

    public static synchronized void startMaydaySiren() {
        if (isAlarmActive) return;
        isAlarmActive = true;

        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            }
        } catch (Exception e) {
            try {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            } catch (Exception ignored) {
                return;
            }
        }

        if (sirenHandler == null) {
            sirenHandler = new Handler(Looper.getMainLooper());
        }

        sirenRunnable = new Runnable() {
            private boolean highPitch = true;

            @Override
            public void run() {
                if (!isAlarmActive || toneGenerator == null) return;
                try {
                    int toneType = highPitch
                            ? ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
                            : ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE;
                    toneGenerator.startTone(toneType, 400);
                    highPitch = !highPitch;
                    sirenHandler.postDelayed(this, 550);
                } catch (Exception ignored) {}
            }
        };

        sirenHandler.post(sirenRunnable);
    }

    public static synchronized void stopMaydaySiren() {
        isAlarmActive = false;
        if (sirenHandler != null && sirenRunnable != null) {
            sirenHandler.removeCallbacks(sirenRunnable);
        }
        if (toneGenerator != null) {
            try {
                toneGenerator.stopTone();
                toneGenerator.release();
            } catch (Exception ignored) {}
            toneGenerator = null;
        }
    }

    public static boolean isAlarmActive() {
        return isAlarmActive;
    }
}

