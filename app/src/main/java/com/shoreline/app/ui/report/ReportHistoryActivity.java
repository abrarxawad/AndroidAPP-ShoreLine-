package com.shoreline.app.ui.report;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.shoreline.app.R;
import com.shoreline.app.data.local.entity.OverloadReportEntity;
import com.shoreline.app.data.repository.ReportRepository;
import com.shoreline.app.databinding.ActivityReportHistoryBinding;
import com.shoreline.app.ui.base.BaseActivity;

import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: RecyclerView 2 & Offline Queue Observer
 * Displays submitted reports, distinguishing between local Room offline queue records and Firestore synced records.
 */
public class ReportHistoryActivity extends BaseActivity {

    private ActivityReportHistoryBinding binding;
    private ReportRepository reportRepository;
    private ReportAdapter reportAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reportRepository = new ReportRepository(this);

        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        loadReports();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.includedToolbar.appToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_report_history);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.includedToolbar.appToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        reportAdapter = new ReportAdapter(report -> {
            String status = report.isSynced() ? "Uploaded to BIWTA Cloud" : "Pending Offline Queue (Waiting for Signal)";
            showToast("Report ID: " + report.getReportId().substring(0, 8) + "\nStatus: " + status);
        });

        binding.rvReports.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReports.setAdapter(reportAdapter);

        binding.btnForceSync.setOnClickListener(v -> {
            reportRepository.forceSyncNow();
            showSnackbar(binding.getRoot(), "Triggered WorkManager background queue upload job.");
            binding.swipeRefreshReports.postDelayed(this::loadReports, 1500);
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshReports.setOnRefreshListener(this::loadReports);
    }

    private void loadReports() {
        binding.swipeRefreshReports.setRefreshing(true);

        reportRepository.getAllReports(new ReportRepository.ReportCallback<List<OverloadReportEntity>>() {
            @Override
            public void onResult(List<OverloadReportEntity> reports) {
                binding.swipeRefreshReports.setRefreshing(false);
                reportAdapter.setReports(reports);

                boolean isEmpty = reports == null || reports.isEmpty();
                binding.layoutEmptyReports.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.rvReports.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                updateQueueBanner(reports);
            }

            @Override
            public void onError(String message) {
                binding.swipeRefreshReports.setRefreshing(false);
                showSnackbar(binding.getRoot(), message);
            }
        });
    }

    private void updateQueueBanner(List<OverloadReportEntity> reports) {
        if (reports == null || reports.isEmpty()) {
            binding.tvQueueStatus.setText("Offline Queue: 0 Pending Reports");
            return;
        }

        int pendingCount = 0;
        for (OverloadReportEntity r : reports) {
            if (!r.isSynced()) pendingCount++;
        }

        if (pendingCount > 0) {
            binding.tvQueueStatus.setText(String.format(Locale.US, "Offline Queue: %d Report(s) Pending Upload", pendingCount));
            binding.tvQueueStatus.setTextColor(getColor(R.color.secondary_safety_orange));
        } else {
            binding.tvQueueStatus.setText("All Reports Successfully Synced to Cloud ✓");
            binding.tvQueueStatus.setTextColor(getColor(R.color.status_safe_text));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReports();
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
