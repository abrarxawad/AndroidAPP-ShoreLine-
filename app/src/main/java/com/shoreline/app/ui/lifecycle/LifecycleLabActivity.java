package com.shoreline.app.ui.lifecycle;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.shoreline.app.R;
import com.shoreline.app.databinding.ActivityLifecycleLabBinding;
import com.shoreline.app.ui.base.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SYLLABUS: Activity Lifecycle Lab & UI Event Listener Showcase
 *
 * Demonstrates:
 * 1. Live visual tracing of Activity lifecycle states (onCreate, onStart, onResume, onPause, onStop, onRestart, onDestroy).
 * 2. State preservation across configuration changes (screen rotation) using onSaveInstanceState & onRestoreInstanceState.
 * 3. 3 Click Listener implementations with in-depth viva comparison (Lambda vs Interface vs XML onClick).
 * 4. TextWatcher implementation (beforeTextChanged, onTextChanged, afterTextChanged).
 * 5. OnLongClickListener with boolean event consumption semantics.
 * 6. CompoundButton.OnCheckedChangeListener on SwitchMaterial.
 * 7. Intent Extras retrieval from calling Activity.
 */
public class LifecycleLabActivity extends BaseActivity implements BaseActivity.LifecycleListener {

    private static final String KEY_ROTATION_COUNT = "STATE_ROTATION_COUNT";
    private static final String KEY_LOG_BUFFER = "STATE_LOG_BUFFER";
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private ActivityLifecycleLabBinding binding;
    private final StringBuilder logBuffer = new StringBuilder();
    private int rotationCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Register self as lifecycle listener before super.onCreate to capture onCreate event
        setLifecycleListener(this);
        super.onCreate(savedInstanceState);

        // SYLLABUS: ViewBinding inflation
        binding = ActivityLifecycleLabBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // SYLLABUS: Reading Intent Extras passed from SplashActivity
        readIntentExtras();

        // SYLLABUS: Restore State if activity was recreated due to rotation/config change
        if (savedInstanceState != null) {
            rotationCount = savedInstanceState.getInt(KEY_ROTATION_COUNT, 0) + 1;
            String savedLogs = savedInstanceState.getString(KEY_LOG_BUFFER, "");
            logBuffer.append(savedLogs);
            appendLog("Recreated: onRestoreInstanceState restored rotationCount = " + rotationCount);
        }
        binding.tvRotationCounter.setText(String.valueOf(rotationCount));

        // Setup UI event listeners
        setupClickListeners();
        setupTextWatcher();
        setupLongClickListener();
        setupCompoundButtonListener();
        setupSimulationNavigation();
    }

    /**
     * SYLLABUS: Intent Extras Parsing
     * Extracts primitive parameters passed across process/activity boundaries.
     */
    private void readIntentExtras() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EXTRA_LAUNCH_TRIGGER")) {
            String trigger = intent.getStringExtra("EXTRA_LAUNCH_TRIGGER");
            long timestamp = intent.getLongExtra("EXTRA_LAUNCH_TIMESTAMP", 0);
            String version = intent.getStringExtra("EXTRA_APP_VERSION");

            String metadata = String.format(Locale.US,
                    "Intent Extras: Triggered by [%s] at %s (App v%s)",
                    trigger, TIME_FMT.format(new Date(timestamp)), version);
            binding.tvLaunchMetadata.setText(metadata);
        } else {
            binding.tvLaunchMetadata.setText(R.string.lab_status_idle);
        }
    }

    // =========================================================================
    // SYLLABUS: Event Listener Showroom (Lambda vs Interface vs XML onClick)
    // =========================================================================
    private void setupClickListeners() {
        // ---------------------------------------------------------------------
        // 1. LAMBDA EXPRESSION (Java 8+)
        // Pros: Highly concise, avoids creating separate anonymous inner class files,
        //       eliminates boilerplate.
        // Cons: Cannot hold multiple methods; captures outer scope references if not careful.
        // ---------------------------------------------------------------------
        binding.btnLambda.setOnClickListener(v -> {
            appendLog("[Lambda Listener] Clicked btnLambda. Clean & modern Java 8 syntax.");
            showSnackbar(binding.getRoot(), "Triggered: Lambda OnClickListener");
        });

        // ---------------------------------------------------------------------
        // 2. VIEW.ONCLICKLISTENER INTERFACE (Anonymous Class)
        // Pros: Standard classical Java pattern. Reusable if stored in a variable,
        //       easy to mock or test in traditional Android unit tests.
        // Cons: Verbose syntax with new View.OnClickListener() boilerplate.
        // ---------------------------------------------------------------------
        binding.btnInterface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appendLog("[Interface Listener] Clicked btnInterface via new View.OnClickListener().");
                showSnackbar(binding.getRoot(), "Triggered: Interface OnClickListener");
            }
        });

        // Clear Log Button
        binding.btnClearLog.setOnClickListener(v -> {
            logBuffer.setLength(0);
            binding.tvLifecycleLog.setText(R.string.lab_status_idle);
        });
    }

    // -------------------------------------------------------------------------
    // 3. XML ATTRIBUTE (android:onClick="onXmlButtonClick")
    // Pros: Quick prototyping in XML layout editor without writing binding in onCreate.
    // Cons: DANGEROUS IN PRODUCTION.
    //       1. Requires method to be public with exact signature (View v).
    //       2. Throws runtime IllegalStateException if renamed without updating XML.
    //       3. Fails inside Fragments (attempts to invoke on Host Activity, not Fragment).
    // -------------------------------------------------------------------------
    public void onXmlButtonClick(View view) {
        appendLog("[XML onClick Listener] Triggered onXmlButtonClick(View) from layout attribute.");
        showSnackbar(binding.getRoot(), "Triggered: android:onClick XML handler");
    }

    // =========================================================================
    // SYLLABUS: TextWatcher Implementation
    // =========================================================================
    private void setupTextWatcher() {
        binding.etStateInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Invoked before characters are replaced
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Invoked during text modification
                String feedback = getString(R.string.lab_text_watcher_prefix) + "\"" + s.toString() + "\" (len: " + s.length() + ")";
                binding.tvTextWatcherFeedback.setText(feedback);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Invoked after text modification is committed
            }
        });
    }

    // =========================================================================
    // SYLLABUS: OnLongClickListener (with Event Consumption)
    // =========================================================================
    private void setupLongClickListener() {
        binding.cardLongPress.setOnLongClickListener(v -> {
            appendLog("[Long-Click Listener] Long press detected on CardView.");
            showToast("CardView: Long-press event detected!");
            // VIVA POINT: Returning 'true' indicates the callback consumed the event.
            // If 'false' was returned, Android would also pass the event down to standard onClick.
            return true;
        });
    }

    // =========================================================================
    // SYLLABUS: CompoundButton.OnCheckedChangeListener (Switch / Checkbox)
    // =========================================================================
    private void setupCompoundButtonListener() {
        binding.switchSync.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            String state = isChecked ? getString(R.string.lab_switch_on) : getString(R.string.lab_switch_off);
            appendLog("[CompoundButton Listener] Switch toggled: " + state);
            showSnackbar(binding.getRoot(), "Background Sync state: " + state);
        });
    }

    // =========================================================================
    // SYLLABUS: Dialog & Navigation (Viva trap: Dialogs vs Lifecycle)
    // =========================================================================
    private void setupSimulationNavigation() {
        binding.btnSimulateNav.setOnClickListener(v -> {
            // VIVA QUESTION: Does showing an AlertDialog cause onPause() to be called?
            // ANSWER: NO! AlertDialog is an in-window dialog attached to the same WindowManager.
            // The hosting Activity remains visible, resumed, and focused in the activity stack.
            new AlertDialog.Builder(this)
                    .setTitle("Viva Concept Check")
                    .setMessage("Notice the Lifecycle Log behind this dialog.\n\n" +
                            "Did onPause() get called? NO!\n\n" +
                            "AlertDialog does NOT trigger onPause() because the hosting Activity remains in the RESUMED state and is not partially occluded by another Activity.")
                    .setPositiveButton(R.string.action_ok, (dialog, which) -> appendLog("Dismissed AlertDialog. onPause was never called."))
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        });
    }

    // =========================================================================
    // SYLLABUS: LifecycleObserver Callback Implementation
    // =========================================================================
    @Override
    public void onLifecycleTransition(String activityName, String eventName, String timestamp, String details) {
        String logEntry = String.format(Locale.US, "[%s] %s() -> %s\n", timestamp, eventName, details != null ? details : "");
        appendLog(logEntry);
    }

    private void appendLog(String message) {
        logBuffer.append(message);
        if (!message.endsWith("\n")) {
            logBuffer.append("\n");
        }
        if (binding != null) {
            binding.tvLifecycleLog.setText(logBuffer.toString());
            // Auto-scroll console to bottom
            binding.consoleScrollView.post(() -> binding.consoleScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    // =========================================================================
    // SYLLABUS: onSaveInstanceState & onRestoreInstanceState
    // =========================================================================
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the rotation counter and the existing terminal log buffer
        outState.putInt(KEY_ROTATION_COUNT, rotationCount);
        outState.putString(KEY_LOG_BUFFER, logBuffer.toString());
        appendLog("[onSaveInstanceState] Saved rotationCount=" + rotationCount + " to Bundle.");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Note: onCreate handles restoration before views are bound, but onRestoreInstanceState
        // is called after onStart() for optional post-start state recovery.
        appendLog("[onRestoreInstanceState] Restored UI state from Bundle.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Prevent Activity View reference leak
    }
}
