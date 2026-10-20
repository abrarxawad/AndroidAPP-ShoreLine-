package com.shoreline.app.ui.report;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.shoreline.app.R;
import com.shoreline.app.data.local.entity.OverloadReportEntity;
import com.shoreline.app.data.repository.ReportRepository;
import com.shoreline.app.databinding.ActivityReportOverloadBinding;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.util.ImageCompressionHelper;
import com.shoreline.app.util.LocationHelper;
import com.shoreline.app.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

/**
 * SYLLABUS: Camera, FileProvider, Runtime Permissions, Fused Location & Offline Queueing
 * Allows river passengers to report dangerous vessel overcrowding with photo evidence and GPS geotag.
 */
public class ReportOverloadActivity extends BaseActivity {

    private ActivityReportOverloadBinding binding;
    private ReportRepository reportRepository;
    private LocationHelper locationHelper;
    private SessionManager sessionManager;

    private double currentLatitude = LocationHelper.DEFAULT_LATITUDE;
    private double currentLongitude = LocationHelper.DEFAULT_LONGITUDE;
    private float currentAccuracy = 20.0f;

    private String base64PhotoData = "";
    private Uri currentCameraPhotoUri = null;
    private File currentPhotoFile = null;

    // SYLLABUS: Modern Activity Result API for Camera Photo Capture
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success) && currentCameraPhotoUri != null) {
                    processImageUri(currentCameraPhotoUri);
                }
            });

    // Gallery Picker Fallback
    private final ActivityResultLauncher<String> galleryPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    processImageUri(uri);
                }
            });

    // SYLLABUS: Modern Runtime Permissions Request
    private final ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLoc = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean camera = result.get(Manifest.permission.CAMERA);

                if (Boolean.TRUE.equals(fineLoc)) {
                    refreshGpsLocation();
                }
                if (Boolean.TRUE.equals(camera)) {
                    dispatchTakePictureIntent();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportOverloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reportRepository = new ReportRepository(this);
        locationHelper = new LocationHelper(this);
        sessionManager = new SessionManager(this);

        setupToolbar();
        handleIntentExtras();
        setupQuickChips();
        setupClickListeners();
        refreshGpsLocation();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.includedToolbar.appToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_report_overload);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.includedToolbar.appToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void handleIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            String vesselReg = intent.getStringExtra("EXTRA_REG_NUMBER");
            String vesselName = intent.getStringExtra("EXTRA_VESSEL_NAME");
            if (!TextUtils.isEmpty(vesselReg)) {
                String display = vesselName != null ? vesselName + " (" + vesselReg + ")" : vesselReg;
                binding.etReportVesselNumber.setText(display);
            }
        }
    }

    private void setupQuickChips() {
        binding.chipParabat.setOnClickListener(v -> binding.etReportVesselNumber.setText("MV Parabat-12 (M-01-2489)"));
        binding.chipSundarban.setOnClickListener(v -> binding.etReportVesselNumber.setText("MV Sundarban-10 (M-01-3104)"));
        binding.chipPubali.setOnClickListener(v -> binding.etReportVesselNumber.setText("MV Pubali-7 [FLAGGED] (M-01-1205)"));
    }

    private void setupClickListeners() {
        binding.btnRefreshGps.setOnClickListener(v -> refreshGpsLocation());

        binding.btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndCapture());

        binding.btnSelectGallery.setOnClickListener(v -> galleryPickerLauncher.launch("image/*"));

        binding.btnSubmitReport.setOnClickListener(v -> validateAndSubmitReport());

        binding.btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ReportOverloadActivity.this, ReportHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void refreshGpsLocation() {
        binding.tvGpsStatus.setText(R.string.gps_fetching);
        locationHelper.getCurrentLocation(new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationAcquired(double latitude, double longitude, float accuracyMeters, boolean isMockOrFallback) {
                currentLatitude = latitude;
                currentLongitude = longitude;
                currentAccuracy = accuracyMeters;

                String fixStatus = isMockOrFallback ? " (River Checkpoint Estimate)" : " (Live GPS Lock)";
                String display = String.format(Locale.US, "GPS: %.4f° N, %.4f° E (±%.1fm)%s",
                        latitude, longitude, accuracyMeters, fixStatus);
                binding.tvGpsStatus.setText(display);
            }

            @Override
            public void onLocationFailed(String reason) {
                binding.tvGpsStatus.setText(String.format("GPS Unavailable: Using Sadarghat (23.7045° N, 90.4131° E)"));
            }
        });
    }

    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsLauncher.launch(new String[]{Manifest.permission.CAMERA});
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        try {
            currentPhotoFile = ImageCompressionHelper.createPhotoFile(this);
            currentCameraPhotoUri = ImageCompressionHelper.getPhotoUri(this, currentPhotoFile);
            takePictureLauncher.launch(currentCameraPhotoUri);
        } catch (IOException e) {
            showToast("Failed to initialize camera cache: " + e.getMessage());
        }
    }

    private void processImageUri(Uri uri) {
        try {
            ImageCompressionHelper.CompressionResult result =
                    ImageCompressionHelper.compressImageUriToBase64(this, uri);
            base64PhotoData = result.getBase64Data();
            binding.ivReportPhotoPreview.setImageBitmap(result.getPreviewBitmap());
            binding.tvPhotoSizeBadge.setText(String.format(Locale.US, "Optimized for 2G: %d KB (Downsampled)", result.getSizeKb()));
            binding.tvPhotoSizeBadge.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            showToast("Image compression failed: " + e.getMessage());
        }
    }

    private void validateAndSubmitReport() {
        String vesselText = binding.etReportVesselNumber.getText() != null ?
                binding.etReportVesselNumber.getText().toString().trim() : "";

        if (TextUtils.isEmpty(vesselText)) {
            binding.etReportVesselNumber.setError("Please enter the launch name or registration number");
            return;
        }

        String passengerCountStr = binding.etPassengerCount.getText() != null ?
                binding.etPassengerCount.getText().toString().trim() : "0";
        int passengerCount;
        try {
            passengerCount = Integer.parseInt(passengerCountStr);
        } catch (NumberFormatException e) {
            passengerCount = 0;
        }

        String crowdLevel = "CRITICAL_OVERLOAD";
        if (binding.rbCrowdNormal.isChecked()) crowdLevel = "LOW";
        else if (binding.rbCrowdModerate.isChecked()) crowdLevel = "MODERATE";

        boolean isAnonymous = binding.switchAnonymous.isChecked();
        String reporterUserId = isAnonymous ? "ANONYMOUS" : sessionManager.getUserId();

        String reportId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        OverloadReportEntity report = new OverloadReportEntity(
                reportId,
                vesselText,
                vesselText,
                crowdLevel,
                passengerCount,
                currentLatitude,
                currentLongitude,
                now,
                base64PhotoData,
                reporterUserId,
                isAnonymous,
                false, // isSynced = false initially
                0
        );

        binding.progressSubmitting.setVisibility(View.VISIBLE);
        binding.btnSubmitReport.setEnabled(false);

        reportRepository.submitReport(report, new ReportRepository.ReportCallback<Boolean>() {
            @Override
            public void onResult(Boolean isUploadedDirectly) {
                binding.progressSubmitting.setVisibility(View.GONE);
                binding.btnSubmitReport.setEnabled(true);

                showSubmissionSuccessDialog(isUploadedDirectly);
            }

            @Override
            public void onError(String message) {
                binding.progressSubmitting.setVisibility(View.GONE);
                binding.btnSubmitReport.setEnabled(true);
                showSnackbar(binding.getRoot(), "Submission error: " + message);
            }
        });
    }

    private void showSubmissionSuccessDialog(boolean isUploadedDirectly) {
        String title = isUploadedDirectly ? "Report Transmitted" : "Saved to Offline Queue";
        String message = isUploadedDirectly ?
                "Evidence has been securely transmitted to the BIWTA Port Authority server with GPS geotag." :
                "Mid-river mobile connectivity is currently weak or offline.\n\nYour overload report with photo evidence is safely stored in the local Room database and will automatically sync via WorkManager once network is restored.";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(isUploadedDirectly ? R.drawable.ic_shoreline_logo : android.R.drawable.ic_dialog_info)
                .setPositiveButton("View Report Queue", (dialog, which) -> {
                    Intent intent = new Intent(ReportOverloadActivity.this, ReportHistoryActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Done", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
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
