package com.shoreline.app.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.shoreline.app.R;
import com.shoreline.app.databinding.ActivityHomeBinding;
import com.shoreline.app.ui.auth.LoginActivity;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.ui.lifecycle.LifecycleLabActivity;
import com.shoreline.app.util.DeckThemeHelper;
import com.shoreline.app.util.HapticHelper;
import com.shoreline.app.util.SessionManager;

/**
 * SYLLABUS: Main Application Dashboard & Role-Based UI Presentation
 * Shows active session state, role-specific feature gating, real-time connectivity telemetry,
 * dynamic Day/Night yacht deck theme, and tactile haptic navigation.
 */
public class HomeActivity extends BaseActivity {

    private ActivityHomeBinding binding;
    private SessionManager sessionManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        // Security check: if not logged in, redirect to login
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        displayUserProfile();
        setupFeatureClicks();
        setupNetworkMonitoring();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayUserProfile();
        DeckThemeHelper.applyDeckBackground(this, binding.ivHomeHeroBackground);
        checkCurrentNetworkState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetworkMonitoring();
    }

    private void displayUserProfile() {
        binding.tvUserName.setText(sessionManager.getFullName());
        binding.tvUserEmail.setText(sessionManager.getEmail());

        boolean isAdmin = sessionManager.isAdmin();
        if (isAdmin) {
            binding.tvRoleBadge.setText(R.string.badge_admin);
            binding.tvRoleBadge.setTextColor(getColor(R.color.secondary_safety_orange));
        } else {
            binding.tvRoleBadge.setText(R.string.badge_passenger);
            binding.tvRoleBadge.setTextColor(getColor(R.color.coastal_cyan));
        }

        // Authority Dashboard is only displayed for authenticated ADMIN users
        binding.cardAdminDashboard.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void setupNetworkMonitoring() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> updateNetworkStatusPill(true));
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> updateNetworkStatusPill(false));
            }
        };

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.registerDefaultNetworkCallback(networkCallback);
            } else {
                NetworkRequest request = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build();
                cm.registerNetworkCallback(request, networkCallback);
            }
        } catch (Exception ignored) {}
    }

    private void checkCurrentNetworkState() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = false;
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network active = cm.getActiveNetwork();
                if (active != null) {
                    NetworkCapabilities caps = cm.getNetworkCapabilities(active);
                    isConnected = (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
                }
            } else {
                android.net.NetworkInfo info = cm.getActiveNetworkInfo();
                isConnected = (info != null && info.isConnected());
            }
        }
        updateNetworkStatusPill(isConnected);
    }

    private void updateNetworkStatusPill(boolean isOnline) {
        if (binding == null) return;
        if (isOnline) {
            binding.viewNetworkStatusDot.setBackgroundResource(R.drawable.bg_dot_online);
            binding.tvNetworkStatusText.setText(R.string.status_cloud_synced);
            binding.tvNetworkStatusText.setTextColor(ContextCompat.getColor(this, R.color.navara_neon_cyan));
        } else {
            binding.viewNetworkStatusDot.setBackgroundResource(R.drawable.bg_dot_offline);
            binding.tvNetworkStatusText.setText(R.string.status_offline_radar);
            binding.tvNetworkStatusText.setTextColor(ContextCompat.getColor(this, R.color.status_warning_amber));
        }
    }

    private void unregisterNetworkMonitoring() {
        if (networkCallback != null) {
            try {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    cm.unregisterNetworkCallback(networkCallback);
                }
            } catch (Exception ignored) {}
            networkCallback = null;
        }
    }

    private void setupFeatureClicks() {
        // Settings & Profile Management Button
        if (binding.btnOpenSettings != null) {
            binding.btnOpenSettings.setOnClickListener(v -> {
                HapticHelper.vibrateClick(v);
                Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.settings.SettingsActivity.class);
                startActivity(intent);
            });
        }

        // AI Maritime Copilot & Chat Assistant
        if (binding.cardAiCopilot != null) {
            binding.cardAiCopilot.setOnClickListener(v -> {
                HapticHelper.vibrateClick(v);
                Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.chat.AiChatActivity.class);
                startActivity(intent);
            });
        }

        // Feature 1: Vessel Verification (Stage 2)
        binding.cardVesselCheck.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.vessel.VesselCheckActivity.class);
            startActivity(intent);
        });

        // Feature 2: Overload Report (Stage 3)
        binding.cardOverloadReport.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.report.ReportOverloadActivity.class);
            startActivity(intent);
        });

        // Feature 3: Safe Route & Weather (Stage 4)
        binding.cardSafeRoute.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.route.SafeRouteActivity.class);
            startActivity(intent);
        });

        // Feature 4: Family Tracker (Stage 5)
        binding.cardFamilyTracker.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.tracker.FamilyTrackerActivity.class);
            startActivity(intent);
        });

        // Feature 5: Offline SOS Beacon (Stage 6)
        binding.cardOfflineSos.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.sos.OfflineSosActivity.class);
            startActivity(intent);
        });

        // Feature 6: Safety Guides & Emergency Directory (Stage 8)
        // SYLLABUS: ContentProvider Consumer, ContentResolver, System Broadcasts
        binding.cardSafetyGuides.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.guide.SafetyGuidesActivity.class);
            startActivity(intent);
        });

        // Feature 7: Admin Dashboard (Stage 7) - Only accessible when logged in as ADMIN
        binding.cardAdminDashboard.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            if (sessionManager.isAdmin()) {
                Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.admin.AdminDashboardActivity.class);
                startActivity(intent);
            }
        });

        // Syllabus requirement: Lifecycle Lab
        binding.btnOpenLifecycleLab.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            Intent intent = new Intent(HomeActivity.this, LifecycleLabActivity.class);
            intent.putExtra("EXTRA_LAUNCH_TRIGGER", "HOME_DASHBOARD");
            startActivity(intent);
        });

        // Floating Maritime Advisory Card Action & Dismiss
        if (binding.btnQuickStartVoyage != null) {
            binding.btnQuickStartVoyage.setOnClickListener(v -> {
                HapticHelper.vibrateClick(v);
                binding.cardSafeRoute.performClick();
            });
        }
        binding.btnAlertAction.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            binding.cardSafeRoute.performClick();
        });
        binding.btnAlertDismiss.setOnClickListener(v -> {
            HapticHelper.vibrateClick(v);
            View card = (View) v.getParent().getParent().getParent();
            if (card != null) {
                card.animate().alpha(0f).setDuration(250).withEndAction(() -> card.setVisibility(View.GONE)).start();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
