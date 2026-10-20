package com.shoreline.app.ui.home;

import android.content.Intent;
import android.os.Bundle;

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

        String kinInfo = "Kin SOS: " + sessionManager.getEmergencyContact();
        binding.tvEmergencyContact.setText(kinInfo);
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
        binding.cardSafeRoute.setOnClickListener(v ->
                showSnackbar(binding.getRoot(), "Safe River Route: Ready for Stage 4 (Dijkstra + Open-Meteo)"));

        // Feature 4: Family Tracker (Stage 5)
        binding.cardFamilyTracker.setOnClickListener(v ->
                showSnackbar(binding.getRoot(), "Family Journey Tracker: Ready for Stage 5 (Live GPS Service)"));

        // Feature 5: Offline SOS Beacon (Stage 6)
        binding.cardOfflineSos.setOnClickListener(v ->
                showSnackbar(binding.getRoot(), "Offline SOS Beacon: Ready for Stage 6 (Nearby Mesh Relay)"));

        // Feature 6: Safety Guides (Stage 8)
        binding.cardSafetyGuides.setOnClickListener(v ->
                showSnackbar(binding.getRoot(), "Emergency Guides: Ready for Stage 8 (Offline Water Rescue)"));

        // Feature 7: Admin Dashboard (Stage 7)
        binding.cardAdminDashboard.setOnClickListener(v -> {
            if (sessionManager.isAdmin()) {
                showSnackbar(binding.getRoot(), "Admin Dashboard: Authorized access (Stage 7 Hotspots)");
            } else {
                showSnackbar(binding.getRoot(), "Notice: Port Authority tools require BIWTA Inspector role.");
            }
        });

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
