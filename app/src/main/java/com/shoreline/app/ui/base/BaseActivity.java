package com.shoreline.app.ui.base;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SYLLABUS: Activity Lifecycle & Base Class Pattern
 * Every activity in ShoreLine extends BaseActivity.
 * It provides:
 * 1. Comprehensive Logcat telemetry for all 7 lifecycle callbacks.
 * 2. An optional LifecycleObserver callback hook for diagnostic UI screens (e.g. LifecycleLabActivity).
 * 3. Uniform Toast and Snackbar presentation helpers conforming to Google Material UX.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private final String TAG = "ShoreLine_Lifecycle_" + getClass().getSimpleName();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    /**
     * Interface allowing activities or test components to observe lifecycle transitions in real-time.
     */
    public interface LifecycleListener {
        void onLifecycleTransition(String activityName, String eventName, String timestamp, String details);
    }

    @Nullable
    private LifecycleListener lifecycleListener;

    public void setLifecycleListener(@Nullable LifecycleListener listener) {
        this.lifecycleListener = listener;
    }

    private void notifyLifecycleEvent(String eventName, String details) {
        String timestamp = TIME_FORMAT.format(new Date());
        Log.d(TAG, "[" + timestamp + "] " + eventName + "() called. " + (details != null ? details : ""));
        if (lifecycleListener != null) {
            lifecycleListener.onLifecycleTransition(getClass().getSimpleName(), eventName, timestamp, details);
        }
    }

    // =========================================================================
    // SYLLABUS: Activity Lifecycle Callbacks
    // =========================================================================

    /**
     * Called when the activity is first created.
     * State initialization, ViewBinding inflation, and saved state restoration occur here.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String details = (savedInstanceState != null) ? "Restoring previous saved state" : "Fresh launch (savedInstanceState == null)";
        notifyLifecycleEvent("onCreate", details);
    }

    /**
     * Called when the activity becomes visible to the user.
     * UI animations, location receivers, or local broadcast listeners can be initiated here.
     */
    @Override
    protected void onStart() {
        super.onStart();
        notifyLifecycleEvent("onStart", "Activity now visible in foreground");
    }

    /**
     * Called when the activity begins interacting with the user.
     * At this point, the activity is at the top of the activity back stack and has user input focus.
     */
    @Override
    protected void onResume() {
        super.onResume();
        notifyLifecycleEvent("onResume", "Activity has input focus (interactive)");
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     * Commit unsaved changes, stop CPU-intensive animations, release camera/sensors.
     * Must be fast; next activity cannot be resumed until onPause completes.
     */
    @Override
    protected void onPause() {
        super.onPause();
        notifyLifecycleEvent("onPause", "Losing input focus (partially occluded or navigating away)");
    }

    /**
     * Called when the activity is no longer visible to the user.
     * Release heavy resources, unregister background broadcast receivers.
     */
    @Override
    protected void onStop() {
        super.onStop();
        notifyLifecycleEvent("onStop", "Activity hidden in background");
    }

    /**
     * Called after the activity has been stopped, just prior to being started again.
     * Always followed by onStart().
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        notifyLifecycleEvent("onRestart", "Activity returning to foreground from stopped state");
    }

    /**
     * Final call received before the activity is destroyed.
     * Occurs due to finish() being invoked or system killing process for memory reclaim.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifyLifecycleEvent("onDestroy", isFinishing() ? "User or finish() initiated destruction" : "System reclaiming memory / configuration change");
        lifecycleListener = null;
    }

    // =========================================================================
    // SYLLABUS: State Preservation & Configuration Changes
    // =========================================================================

    /**
     * Invoked to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in onCreate(Bundle) or onRestoreInstanceState(Bundle).
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        notifyLifecycleEvent("onSaveInstanceState", "Preserving transient UI state into Bundle");
    }

    /**
     * Called after onStart() when the activity is being re-initialized from a previously saved state.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        notifyLifecycleEvent("onRestoreInstanceState", "Restoring UI state from saved Bundle");
    }

    // =========================================================================
    // SYLLABUS: Toast and Snackbar UX Helpers
    // =========================================================================

    /**
     * Displays a short Toast message.
     * Use case: Brief, non-critical background notifications not tied to a specific view.
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays a Material Snackbar message.
     * Use case: Contextual interactive alert anchored to the active layout view hierarchy.
     */
    protected void showSnackbar(View anchorView, String message) {
        if (anchorView != null) {
            Snackbar.make(anchorView, message, Snackbar.LENGTH_SHORT).show();
        } else {
            showToast(message);
        }
    }
}
