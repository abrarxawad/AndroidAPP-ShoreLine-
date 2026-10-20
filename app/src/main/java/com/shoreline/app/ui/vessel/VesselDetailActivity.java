package com.shoreline.app.ui.vessel;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.shoreline.app.R;
import com.shoreline.app.databinding.ActivityVesselDetailBinding;
import com.shoreline.app.ui.base.BaseActivity;

import java.util.Locale;

/**
 * SYLLABUS: Vessel Details & Custom Capacity Gauge Presentation
 * Integrates CapacityGaugeView, renders safety compliance badges, and initiates overload reporting.
 */
public class VesselDetailActivity extends BaseActivity {

    private ActivityVesselDetailBinding binding;

    private String vesselId;
    private String regNumber;
    private String vesselName;
    private String routeName;
    private int capacity;
    private String fitnessExpiry;
    private float rating;
    private int reportCount;
    private boolean isFlagged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVesselDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        readIntentExtras();
        setupToolbar();
        populateVesselDetails();
        setupActionListeners();
    }

    private void readIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            vesselId = intent.getStringExtra("EXTRA_VESSEL_ID");
            regNumber = intent.getStringExtra("EXTRA_REG_NUMBER");
            vesselName = intent.getStringExtra("EXTRA_VESSEL_NAME");
            routeName = intent.getStringExtra("EXTRA_ROUTE_NAME");
            capacity = intent.getIntExtra("EXTRA_CAPACITY", 500);
            fitnessExpiry = intent.getStringExtra("EXTRA_FITNESS_EXPIRY");
            rating = intent.getFloatExtra("EXTRA_RATING", 4.0f);
            reportCount = intent.getIntExtra("EXTRA_REPORTS", 0);
            isFlagged = intent.getBooleanExtra("EXTRA_IS_FLAGGED", false);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.includedToolbar.appToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(vesselName != null ? vesselName : "Vessel Verification");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.includedToolbar.appToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void populateVesselDetails() {
        binding.tvDetailVesselName.setText(vesselName);
        binding.tvDetailRegNumber.setText(String.format("BIWTA Registration: %s", regNumber));
        binding.tvDetailRoute.setText(String.format("Route: %s", routeName));
        binding.tvDetailCapacity.setText(String.format(Locale.US, "%d Passengers", capacity));
        binding.tvDetailRating.setText(String.format(Locale.US, "★ %.1f / 5.0 (%d Reports)", rating, reportCount));

        // Fitness Certificate Evaluation
        if (isFlagged) {
            binding.tvFlaggedAlert.setVisibility(View.VISIBLE);
            binding.tvDetailFitness.setText(String.format("%s (EXPIRED / SUSPENDED)", fitnessExpiry));
            binding.tvDetailFitness.setTextColor(ContextCompat.getColor(this, R.color.status_danger_red));
        } else {
            binding.tvFlaggedAlert.setVisibility(View.GONE);
            binding.tvDetailFitness.setText(String.format("%s (OFFICIALLY VALID)", fitnessExpiry));
            binding.tvDetailFitness.setTextColor(ContextCompat.getColor(this, R.color.status_safe_green));
        }

        // =====================================================================
        // SYLLABUS: Custom View Configuration (CapacityGaugeView)
        // Default simulated load is 72% of licensed capacity (Safe green)
        // Examiners can touch / slide across the gauge to test amber and red overload
        // =====================================================================
        int initialLoad = isFlagged ? Math.round(capacity * 1.15f) : Math.round(capacity * 0.72f);
        binding.capacityGauge.setCapacityAndLoad(capacity, initialLoad);
    }

    private void setupActionListeners() {
        binding.btnReportThisVessel.setOnClickListener(v -> {
            Intent intent = new Intent(VesselDetailActivity.this, com.shoreline.app.ui.report.ReportOverloadActivity.class);
            intent.putExtra("EXTRA_VESSEL_ID", vesselId);
            intent.putExtra("EXTRA_REG_NUMBER", regNumber);
            intent.putExtra("EXTRA_VESSEL_NAME", vesselName);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
