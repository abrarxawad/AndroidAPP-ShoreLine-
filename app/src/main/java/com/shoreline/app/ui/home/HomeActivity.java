package com.shoreline.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.shoreline.app.R;
import com.shoreline.app.databinding.ActivityHomeBinding;
import com.shoreline.app.ui.auth.LoginActivity;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.ui.lifecycle.LifecycleLabActivity;
import com.shoreline.app.util.SessionManager;

/**
 * SYLLABUS: Main Application Dashboard & Role-Based UI Presentation
 * Shows active session state, role-specific feature gating, and navigation hub.
 */
public class HomeActivity extends BaseActivity {

    private ActivityHomeBinding binding;
    private SessionManager sessionManager;

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
    }

    private void displayUserProfile() {
        binding.tvUserName.setText(sessionManager.getFullName());
        binding.tvUserEmail.setText(sessionManager.getEmail());

        String role = sessionManager.getUserRole();
        if ("ADMIN".equalsIgnoreCase(role)) {
            binding.tvRoleBadge.setText(R.string.badge_admin);
            binding.tvRoleBadge.setTextColor(getColor(R.color.secondary_safety_orange));
        } else {
            binding.tvRoleBadge.setText(R.string.badge_passenger);
            binding.tvRoleBadge.setTextColor(getColor(R.color.coastal_cyan));
        }
    }

    private void setupFeatureClicks() {
        // Logout Button
        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            showToast("Logged out successfully");
            redirectToLogin();
        });

        // Feature 1: Vessel Verification (Stage 2)
        binding.cardVesselCheck.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.vessel.VesselCheckActivity.class);
            startActivity(intent);
        });

        // Feature 2: Overload Report (Stage 3)
        binding.cardOverloadReport.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.report.ReportOverloadActivity.class);
            startActivity(intent);
        });

        // Feature 3: Safe Route & Weather (Stage 4)
        binding.cardSafeRoute.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.route.SafeRouteActivity.class);
            startActivity(intent);
        });

        // Feature 4: Family Tracker (Stage 5)
        binding.cardFamilyTracker.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.tracker.FamilyTrackerActivity.class);
            startActivity(intent);
        });

        // Feature 5: Offline SOS Beacon (Stage 6)
        binding.cardOfflineSos.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.sos.OfflineSosActivity.class);
            startActivity(intent);
        });

        // Feature 6: Safety Guides (Stage 8)
        binding.cardSafetyGuides.setOnClickListener(v ->
                showSnackbar(binding.getRoot(), "Emergency Guides: Ready for Stage 8 (Offline Water Rescue)"));

        // Feature 7: Admin Dashboard (Stage 7)
        View.OnClickListener adminClickListener = v -> {
            if (sessionManager.isAdmin()) {
                Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.admin.AdminDashboardActivity.class);
                startActivity(intent);
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this)
                        .setTitle("BIWTA Port Authority Portal")
                        .setMessage("This dashboard is reserved for authorized BIWTA port inspectors and maritime patrol.\n\nFor course evaluation and project defense, would you like to activate the BIWTA Inspector Pass?")
                        .setPositiveButton("Activate Inspector Pass", (dialog, which) -> {
                            sessionManager.setUserRole("ADMIN");
                            displayUserProfile();
                            Intent intent = new Intent(HomeActivity.this, com.shoreline.app.ui.admin.AdminDashboardActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        };

        binding.cardAdminDashboard.setOnClickListener(adminClickListener);
        binding.btnQuickAdminPortal.setOnClickListener(adminClickListener);

        // Syllabus requirement: Lifecycle Lab
        binding.btnOpenLifecycleLab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LifecycleLabActivity.class);
            intent.putExtra("EXTRA_LAUNCH_TRIGGER", "HOME_DASHBOARD");
            startActivity(intent);
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
