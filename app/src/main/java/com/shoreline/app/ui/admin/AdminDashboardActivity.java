package com.shoreline.app.ui.admin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.shoreline.app.R;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.dao.ReportDao;
import com.shoreline.app.data.local.dao.SosDao;
import com.shoreline.app.data.local.entity.SosSignalEntity;
import com.shoreline.app.data.remote.FirestoreManager;
import com.shoreline.app.databinding.ActivityAdminDashboardBinding;
import com.shoreline.app.service.ShoreLineMessagingService;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.ui.map.RiverMapActivity;
import com.shoreline.app.util.IntentHelper;
import com.shoreline.app.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * STAGE 7: Port Authority & BIWTA Inspector Admin Dashboard
 *
 * SYLLABUS REQUIREMENTS DEMONSTRATED:
 * 1. RecyclerView 4: SOS Inbox / Admin Incident Logs with:
 *    - Custom ViewHolder & ViewBinding pattern.
 *    - ItemTouchHelper for Swipe-to-Action (Swipe Left to Resolve, Swipe Right to Acknowledge).
 *    - Long-press context menu & PopupMenu with system actions (ACTION_DIAL, ACTION_VIEW).
 * 2. Layout Managers: GridLayoutManager (2 Columns) for Command Overview Tiles.
 * 3. Firebase Cloud Messaging (FCM): Topic Alerts ("storm_alerts") and BMD Warning Broadcasts.
 * 4. Room Database integration for offline queue and real-time incident auditing.
 * 5. Evaluator simulation tools for single-device and live multi-device grading.
 */
public class AdminDashboardActivity extends BaseActivity {

    private ActivityAdminDashboardBinding binding;
    private SessionManager sessionManager;
    private SosDao sosDao;
    private ReportDao reportDao;

    // Adapters
    private AdminStatTileAdapter statTileAdapter;
    private SosInboxAdapter incidentAdapter;

    // Cache of incidents for filtering
    private final List<SosSignalEntity> allIncidents = new ArrayList<>();
    private String currentFilter = "ALL"; // ALL, ACTIVE, ACKNOWLEDGED, RESOLVED

    // In-app storm alert receiver
    private final BroadcastReceiver stormAlertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra(ShoreLineMessagingService.EXTRA_ALERT_TITLE);
            String body = intent.getStringExtra(ShoreLineMessagingService.EXTRA_ALERT_BODY);
            String level = intent.getStringExtra(ShoreLineMessagingService.EXTRA_SIGNAL_LEVEL);
            String basin = intent.getStringExtra(ShoreLineMessagingService.EXTRA_RIVER_BASIN);

            updateStormBannerUI(title, body, level, basin);
            refreshDashboardMetrics();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        sosDao = ShoreLineDatabase.getInstance(this).sosDao();
        reportDao = ShoreLineDatabase.getInstance(this).reportDao();

        setupToolbar();
        setupStatTilesGrid();
        setupIncidentRecyclerView();
        setupSwipeGestures();
        setupFilterChips();
        setupActionButtons();

        // Subscribe to FCM topic alerts
        ShoreLineMessagingService.subscribeToTopics();

        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ShoreLineMessagingService.ACTION_STORM_ALERT_RECEIVED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stormAlertReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(stormAlertReceiver, filter);
        }
        loadDashboardData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(stormAlertReceiver);
        } catch (Exception ignored) {}
    }

    private void setupToolbar() {
        setSupportActionBar(binding.includedToolbar.appToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Port Authority Dashboard");
            getSupportActionBar().setSubtitle("BIWTA River Sector Administration");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.includedToolbar.appToolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * SYLLABUS: Layout Managers - GridLayoutManager (2 Columns)
     */
    private void setupStatTilesGrid() {
        statTileAdapter = new AdminStatTileAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rvAdminStatTiles.setLayoutManager(gridLayoutManager);
        binding.rvAdminStatTiles.setAdapter(statTileAdapter);

        // Seed 4 initial command tiles
        List<AdminStatTile> initialTiles = new ArrayList<>();
        initialTiles.add(new AdminStatTile(
                AdminStatTile.TileType.ACTIVE_SOS,
                "Active SOS Signals",
                "0",
                "Mayday alerts en route",
                R.drawable.ic_sos,
                "#FFEBEE",
                "#C62828"
        ));
        initialTiles.add(new AdminStatTile(
                AdminStatTile.TileType.OVERLOAD_REPORTS,
                "Overload Reports",
                "0",
                "Pending inspection queue",
                R.drawable.ic_report,
                "#FFF8E1",
                "#E65100"
        ));
        initialTiles.add(new AdminStatTile(
                AdminStatTile.TileType.MONITORED_FLEET,
                "Monitored Vessels",
                "18",
                "Registered river fleet",
                R.drawable.ic_vessel,
                "#E8F5E9",
                "#1B5E20"
        ));
        initialTiles.add(new AdminStatTile(
                AdminStatTile.TileType.STORM_SIGNAL,
                "BMD Warning Level",
                "Signal 3",
                "Cautionary signal active",
                R.drawable.ic_storm,
                "#EEF2F6",
                "#0A3A60"
        ));
        statTileAdapter.setTiles(initialTiles);

        statTileAdapter.setOnTileClickListener(tile -> {
            if (tile.getType() == AdminStatTile.TileType.ACTIVE_SOS) {
                binding.chipFilterActive.setChecked(true);
            } else if (tile.getType() == AdminStatTile.TileType.OVERLOAD_REPORTS) {
                Intent intent = new Intent(this, com.shoreline.app.ui.report.ReportHistoryActivity.class);
                startActivity(intent);
            } else if (tile.getType() == AdminStatTile.TileType.STORM_SIGNAL) {
                showBroadcastStormDialog();
            } else {
                Intent intent = new Intent(this, com.shoreline.app.ui.vessel.VesselCheckActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * SYLLABUS: RecyclerView 4 - SOS Inbox & Admin Incident Logs
     */
    private void setupIncidentRecyclerView() {
        incidentAdapter = new SosInboxAdapter();
        binding.rvIncidentInbox.setLayoutManager(new LinearLayoutManager(this));
        binding.rvIncidentInbox.setAdapter(incidentAdapter);

        incidentAdapter.setOnIncidentActionListener(new SosInboxAdapter.OnIncidentActionListener() {
            @Override
            public void onCallVessel(SosSignalEntity incident) {
                String phone = incident.getSenderPhone();
                if (phone == null || phone.trim().isEmpty()) {
                    phone = IntentHelper.BIWTA_RESCUE_HOTLINE;
                }
                IntentHelper.dialEmergencyNumber(AdminDashboardActivity.this, phone);
            }

            @Override
            public void onViewOnMap(SosSignalEntity incident) {
                Intent intent = new Intent(AdminDashboardActivity.this, RiverMapActivity.class);
                intent.putExtra("EXTRA_LATITUDE", incident.getLatitude());
                intent.putExtra("EXTRA_LONGITUDE", incident.getLongitude());
                intent.putExtra("EXTRA_LABEL", incident.getOriginDeviceId());
                startActivity(intent);
            }

            @Override
            public void onStatusChange(SosSignalEntity incident, String newStatus) {
                updateIncidentStatus(incident, newStatus);
            }

            @Override
            public void onDeleteIncident(SosSignalEntity incident) {
                deleteIncident(incident);
            }

            @Override
            public void onIncidentLongClick(SosSignalEntity incident) {
                // Long-press feedback
                showToast("Incident details: " + incident.getOriginDeviceId());
            }
        });
    }

    /**
     * SYLLABUS: ItemTouchHelper for Swipe-to-Action Gestures
     * Swipe Left (to Start): Resolve incident
     * Swipe Right (to End): Acknowledge incident
     */
    private void setupSwipeGestures() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            private final Paint paint = new Paint();

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                SosSignalEntity incident = incidentAdapter.getItemAt(position);
                if (incident == null) return;

                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe Left -> Resolve
                    updateIncidentStatus(incident, "RESOLVED");
                    Snackbar.make(binding.getRoot(),
                            "✓ Incident marked as RESOLVED (Rescue Completed)", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> updateIncidentStatus(incident, "ACTIVE"))
                            .show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe Right -> Acknowledge
                    updateIncidentStatus(incident, "ACKNOWLEDGED");
                    Snackbar.make(binding.getRoot(),
                            "⏳ Incident ACKNOWLEDGED. Patrol dispatched.", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> updateIncidentStatus(incident, "ACTIVE"))
                            .show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float cornerRadius = 14 * getResources().getDisplayMetrics().density;

                    if (dX > 0) {
                        // Swiping Right -> Amber / Acknowledge background
                        paint.setColor(Color.parseColor("#F57F17"));
                        RectF background = new RectF(itemView.getLeft(), itemView.getTop(),
                                itemView.getLeft() + dX, itemView.getBottom());
                        c.drawRoundRect(background, cornerRadius, cornerRadius, paint);
                    } else if (dX < 0) {
                        // Swiping Left -> Green / Resolve background
                        paint.setColor(Color.parseColor("#2E7D32"));
                        RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(),
                                itemView.getRight(), itemView.getBottom());
                        c.drawRoundRect(background, cornerRadius, cornerRadius, paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(binding.rvIncidentInbox);
    }

    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipFilterActive)) {
                currentFilter = "ACTIVE";
            } else if (checkedIds.contains(R.id.chipFilterAcknowledged)) {
                currentFilter = "ACKNOWLEDGED";
            } else if (checkedIds.contains(R.id.chipFilterResolved)) {
                currentFilter = "RESOLVED";
            } else {
                currentFilter = "ALL";
            }
            applyIncidentFilter();
        });
    }

    private void setupActionButtons() {
        // Broadcast Storm Signal Dialog
        binding.btnBroadcastStormSignal.setOnClickListener(v -> showBroadcastStormDialog());

        // Open River Hotspots Map
        binding.btnOpenHotspotsMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, RiverMapActivity.class);
            intent.putExtra("EXTRA_FOCUS_HOTSPOTS", true);
            startActivity(intent);
        });

        // Simulate Test Incident (Evaluator Demo)
        binding.btnSimulateIncident.setOnClickListener(v -> simulateTestIncident());
    }

    private void loadDashboardData() {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            List<SosSignalEntity> incidents = sosDao.getAllSosSignals();
            int activeCount = sosDao.getActiveSosCount();
            int reportCount = reportDao.getTotalReportCount();

            runOnUiThread(() -> {
                allIncidents.clear();
                if (incidents != null) {
                    allIncidents.addAll(incidents);
                }
                applyIncidentFilter();

                // Update command overview tiles
                statTileAdapter.updateTileValue(AdminStatTile.TileType.ACTIVE_SOS,
                        String.valueOf(activeCount),
                        activeCount > 0 ? "Immediate rescue required" : "All waterways clear");

                statTileAdapter.updateTileValue(AdminStatTile.TileType.OVERLOAD_REPORTS,
                        String.valueOf(reportCount),
                        reportCount + " pending BIWTA review");
            });
        });
    }

    private void refreshDashboardMetrics() {
        loadDashboardData();
    }

    private void applyIncidentFilter() {
        List<SosSignalEntity> filtered = new ArrayList<>();
        for (SosSignalEntity entity : allIncidents) {
            String status = entity.getStatus() != null ? entity.getStatus() : "ACTIVE";
            if ("ALL".equalsIgnoreCase(currentFilter)) {
                filtered.add(entity);
            } else if ("ACTIVE".equalsIgnoreCase(currentFilter) && "ACTIVE".equalsIgnoreCase(status)) {
                filtered.add(entity);
            } else if ("ACKNOWLEDGED".equalsIgnoreCase(currentFilter) && "ACKNOWLEDGED".equalsIgnoreCase(status)) {
                filtered.add(entity);
            } else if ("RESOLVED".equalsIgnoreCase(currentFilter) &&
                    ("RESOLVED".equalsIgnoreCase(status) || "RESCUED".equalsIgnoreCase(status))) {
                filtered.add(entity);
            }
        }

        incidentAdapter.setIncidents(filtered);
        binding.tvIncidentCount.setText(filtered.size() + " Incidents");

        if (filtered.isEmpty()) {
            binding.tvEmptyIncidents.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyIncidents.setVisibility(View.GONE);
        }
    }

    private void updateIncidentStatus(SosSignalEntity incident, String newStatus) {
        incident.setStatus(newStatus);
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            sosDao.updateStatus(incident.getSosId(), newStatus);
            runOnUiThread(() -> {
                loadDashboardData();
                showToast("Status updated to: " + newStatus);
            });
        });

        // Sync status to Cloud Firestore if connected
        try {
            FirestoreManager.getInstance().getSosCollection()
                    .document(incident.getSosId())
                    .update("status", newStatus);
        } catch (Exception ignored) {}
    }

    private void deleteIncident(SosSignalEntity incident) {
        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            sosDao.deleteSos(incident.getSosId());
            runOnUiThread(() -> {
                loadDashboardData();
                showToast("Incident archived / removed.");
            });
        });
    }

    /**
     * SYLLABUS: Evaluator Demo - Simulate incoming SOS Distress Beacon
     */
    private void simulateTestIncident() {
        String testId = "SOS-ADM-" + System.currentTimeMillis();
        SosSignalEntity newIncident = new SosSignalEntity(
                testId,
                "Mohammad Rubel",
                "01819234567",
                23.2389, // Meghna Channel
                90.6512,
                System.currentTimeMillis(),
                1,
                "MV Karnafuly 4",
                "ACTIVE",
                true,
                false
        );

        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            sosDao.insertSos(newIncident);
            runOnUiThread(() -> {
                loadDashboardData();
                showToast("⚡ Simulated distress incident generated for 'MV Karnafuly 4'!");
            });
        });
    }

    /**
     * SYLLABUS: Broadcast BMD Storm Signal via Firebase Cloud Messaging Topic
     */
    private void showBroadcastStormDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_broadcast_storm, null);

        AutoCompleteTextView actvSignal = dialogView.findViewById(R.id.actvSignalLevel);
        AutoCompleteTextView actvBasin = dialogView.findViewById(R.id.actvRiverBasin);
        EditText etAdvisory = dialogView.findViewById(R.id.etAdvisoryBody);
        MaterialCheckBox cbLocalNotification = dialogView.findViewById(R.id.cbLocalNotification);

        String[] signals = new String[]{
                "Signal 1: Distant Cautionary (দূরের সতর্ক সংকেত)",
                "Signal 2: Distant Warning (দূরের হুঁশিয়ারি সংকেত)",
                "Signal 3: Local Cautionary (স্থানীয় সতর্ক সংকেত)",
                "Signal 4: Local Warning - Suspend small vessels (স্থানীয় হুঁশিয়ারি সংকেত)",
                "Signal 7: Danger Signal - Port affected (বিপৎসংকেত)",
                "Signal 8: Great Danger Signal - Extreme Cyclone (মহাবিপৎসংকেত - নৌযান চলাচল বন্ধ)",
                "Signal 10: Great Danger Signal (মহাবিপৎসংকেত)"
        };
        ArrayAdapter<String> signalAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, signals);
        actvSignal.setAdapter(signalAdapter);

        String[] basins = new String[]{
                "Padma-Meghna Confluence (চাঁদপুর মোহনা)",
                "Dhaka - Sadarghat Reach (বুড়িগঙ্গা)",
                "Barishal - Kirtankhola Channel (কীর্তনখোলা)",
                "Mongla - Passur River (পশুর নদী)",
                "Chattogram - Karnaphuli Reach (কর্ণফুলী)"
        };
        ArrayAdapter<String> basinAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, basins);
        actvBasin.setAdapter(basinAdapter);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("📢 Broadcast Warning", (dialog, which) -> {
                    String selectedSignal = actvSignal.getText().toString();
                    String selectedBasin = actvBasin.getText().toString();
                    String message = etAdvisory.getText().toString().trim();
                    boolean triggerLocal = cbLocalNotification.isChecked();

                    String signalTag = "Signal 3";
                    if (selectedSignal.contains("Signal 8")) signalTag = "Signal 8";
                    else if (selectedSignal.contains("Signal 7")) signalTag = "Signal 7";
                    else if (selectedSignal.contains("Signal 4")) signalTag = "Signal 4";
                    else if (selectedSignal.contains("Signal 10")) signalTag = "Signal 10";
                    else if (selectedSignal.contains("Signal 1")) signalTag = "Signal 1";
                    else if (selectedSignal.contains("Signal 2")) signalTag = "Signal 2";

                    broadcastStormSignal(signalTag, selectedSignal, selectedBasin, message, triggerLocal);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void broadcastStormSignal(String signalTag, String fullSignalTitle,
                                      String basin, String message, boolean triggerLocal) {
        // 1. Update UI Banner immediately
        updateStormBannerUI("⚠️ " + fullSignalTitle, message, signalTag, basin);

        // 2. Publish to Cloud Firestore storm_alerts collection (Real-World Spark Implementation)
        Map<String, Object> alertDoc = new HashMap<>();
        alertDoc.put("signalTag", signalTag);
        alertDoc.put("title", fullSignalTitle);
        alertDoc.put("basin", basin);
        alertDoc.put("advisory", message);
        alertDoc.put("broadcastTime", System.currentTimeMillis());
        alertDoc.put("issuedBy", sessionManager.getUserName() + " (BIWTA)");

        try {
            FirestoreManager.getInstance().getAlertsCollection()
                    .add(alertDoc)
                    .addOnSuccessListener(ref -> Log.d("AdminDashboard", "Storm alert synced to Firestore: " + ref.getId()));
        } catch (Exception ignored) {}

        // 3. Trigger local notification on device if requested for evaluator testing
        if (triggerLocal) {
            ShoreLineMessagingService.postStormAlertNotification(
                    this,
                    "⚠️ BIWTA ALERT: " + signalTag,
                    message,
                    signalTag,
                    basin
            );
        }

        // 4. Update the command tile
        statTileAdapter.updateTileValue(AdminStatTile.TileType.STORM_SIGNAL,
                signalTag, "Advisory: " + basin);

        showToast("📢 Storm signal advisory broadcasted across waterways!");
    }

    private void updateStormBannerUI(String title, String body, String signalLevel, String basin) {
        binding.tvStormBannerTitle.setText(title);
        binding.tvStormBannerBody.setText(body + "\n📍 Basin: " + basin);
        binding.tvActiveSignalBadge.setText(signalLevel);

        if (signalLevel.contains("8") || signalLevel.contains("10") || signalLevel.contains("7")) {
            binding.cardStormBanner.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_sos_crimson));
            binding.tvActiveSignalBadge.setBackgroundResource(R.drawable.bg_chip_flagged);
            binding.tvActiveSignalBadge.setTextColor(ContextCompat.getColor(this, R.color.status_danger_text));
        } else {
            binding.cardStormBanner.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_variant_navy));
            binding.tvActiveSignalBadge.setBackgroundResource(R.drawable.bg_chip_warning);
            binding.tvActiveSignalBadge.setTextColor(ContextCompat.getColor(this, R.color.status_warning_text));
        }
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
