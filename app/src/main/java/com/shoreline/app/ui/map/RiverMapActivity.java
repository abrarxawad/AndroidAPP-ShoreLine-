package com.shoreline.app.ui.map;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.shoreline.app.R;
import com.shoreline.app.data.model.GhatNode;
import com.shoreline.app.data.model.WaterwayEdge;
import com.shoreline.app.databinding.ActivityRiverMapBinding;
import com.shoreline.app.ui.base.BaseActivity;
import com.shoreline.app.util.GhatXmlParser;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * SYLLABUS: OpenStreetMap (osmdroid) - 100% Free Open-Source Interactive River Map
 * Accurately plots riverbed waterway channel curves (Buriganga, Meghna, Padma, Kirtankhola, Rupsha)
 * ensuring the navigation line flows exclusively through water instead of cutting across land.
 */
public class RiverMapActivity extends BaseActivity {

    private ActivityRiverMapBinding binding;
    private GhatXmlParser.RiverNetworkData networkData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SYLLABUS: osmdroid configuration with application context and custom User-Agent
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityRiverMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        networkData = GhatXmlParser.parseRiverNetwork(this);

        setupToolbar();
        setupMapView();
        plotRiverRoute();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.includedToolbar.appToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Interactive River Route");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.includedToolbar.appToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupMapView() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        binding.mapView.setMultiTouchControls(true);

        // Default center: Chandpur / Padma-Meghna confluence
        GeoPoint centerPoint = new GeoPoint(23.2321, 90.6473);
        binding.mapView.getController().setZoom(8.5);
        binding.mapView.getController().setCenter(centerPoint);
    }

    private void plotRiverRoute() {
        Intent intent = getIntent();
        if (intent == null) return;

        ArrayList<String> ghatIds = intent.getStringArrayListExtra("EXTRA_ROUTE_GHAT_IDS");
        String algo = intent.getStringExtra("EXTRA_ALGORITHM");
        double distanceKm = intent.getDoubleExtra("EXTRA_DISTANCE", 0.0);
        int timeMinutes = intent.getIntExtra("EXTRA_TIME", 0);
        String weatherNotice = intent.getStringExtra("EXTRA_NOTICE");

        if (algo != null) {
            binding.tvMapAlgoBadge.setText(algo.toUpperCase(Locale.US));
        }

        if (weatherNotice != null) {
            binding.tvMapWeatherNotice.setText(weatherNotice);
        }

        binding.tvMapStats.setText(String.format(Locale.US,
                "Waterway Distance: %.1f km | Est. Steamer Time: %d mins (~%.1f hrs)",
                distanceKm, timeMinutes, (timeMinutes / 60.0)));

        if (ghatIds == null || ghatIds.size() < 2) return;

        // Clear existing overlays
        binding.mapView.getOverlays().clear();

        // Trace the exact riverbed curve geometry
        List<GeoPoint> waterPolylinePoints = new ArrayList<>();
        double minLat = 90.0, maxLat = -90.0, minLon = 180.0, maxLon = -180.0;

        for (int i = 0; i < ghatIds.size() - 1; i++) {
            String fromId = ghatIds.get(i);
            String toId = ghatIds.get(i + 1);

            GhatNode fromNode = networkData.ghatMap.get(fromId);
            GhatNode toNode = networkData.ghatMap.get(toId);
            if (fromNode == null || toNode == null) continue;

            // Start node of this segment
            GeoPoint startPoint = new GeoPoint(fromNode.getLatitude(), fromNode.getLongitude());
            if (waterPolylinePoints.isEmpty()) {
                waterPolylinePoints.add(startPoint);
            }

            // Find connecting waterway edge
            WaterwayEdge edge = findEdge(fromId, toId);
            if (edge != null && !edge.getRiverWaypoints().isEmpty()) {
                List<double[]> waypoints = new ArrayList<>(edge.getRiverWaypoints());

                // If traversing reverse direction (to -> from), reverse the waypoints
                if (edge.getToGhatId().equals(fromId)) {
                    Collections.reverse(waypoints);
                }

                // Append intermediate river curves
                for (double[] wp : waypoints) {
                    GeoPoint pt = new GeoPoint(wp[0], wp[1]);
                    waterPolylinePoints.add(pt);
                }
            }

            // End node of this segment
            GeoPoint endPoint = new GeoPoint(toNode.getLatitude(), toNode.getLongitude());
            waterPolylinePoints.add(endPoint);
        }

        // Add markers for all ghats along the path
        for (int i = 0; i < ghatIds.size(); i++) {
            String id = ghatIds.get(i);
            GhatNode node = networkData.ghatMap.get(id);
            if (node == null) continue;

            GeoPoint pt = new GeoPoint(node.getLatitude(), node.getLongitude());

            minLat = Math.min(minLat, pt.getLatitude());
            maxLat = Math.max(maxLat, pt.getLatitude());
            minLon = Math.min(minLon, pt.getLongitude());
            maxLon = Math.max(maxLon, pt.getLongitude());

            Marker marker = new Marker(binding.mapView);
            marker.setPosition(pt);
            marker.setTitle(node.getName());

            String role;
            if (i == 0) {
                role = "🟢 Departure Port (" + node.getRiver() + " River)";
            } else if (i == ghatIds.size() - 1) {
                role = "🏁 Destination Terminal (" + node.getRiver() + " River)";
            } else {
                role = "⚓ Waypoint Confluence (" + node.getRiver() + " River)";
            }

            marker.setSnippet(role + "\nDivision: " + node.getDivision());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_shoreline_logo));
            binding.mapView.getOverlays().add(marker);
        }

        // Update Card Title
        GhatNode startGhat = networkData.ghatMap.get(ghatIds.get(0));
        GhatNode endGhat = networkData.ghatMap.get(ghatIds.get(ghatIds.size() - 1));
        if (startGhat != null && endGhat != null) {
            binding.tvMapRouteTitle.setText(String.format("%s ➔ %s (River Channel)",
                    startGhat.getName(), endGhat.getName()));
        }

        // Calculate BoundingBox over all river channel waypoints
        for (GeoPoint pt : waterPolylinePoints) {
            minLat = Math.min(minLat, pt.getLatitude());
            maxLat = Math.max(maxLat, pt.getLatitude());
            minLon = Math.min(minLon, pt.getLongitude());
            maxLon = Math.max(maxLon, pt.getLongitude());
        }

        // Draw Dual-Layer High-Contrast River Waterway Channel
        if (waterPolylinePoints.size() >= 2) {
            // Layer 1: Dark Maritime Deep Border
            Polyline outerPolyline = new Polyline();
            outerPolyline.setPoints(waterPolylinePoints);
            outerPolyline.getOutlinePaint().setColor(Color.parseColor("#0A3A60")); // Deep Maritime Navy
            outerPolyline.getOutlinePaint().setStrokeWidth(16.0f);
            outerPolyline.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
            outerPolyline.getOutlinePaint().setStrokeJoin(Paint.Join.ROUND);
            binding.mapView.getOverlays().add(outerPolyline);

            // Layer 2: Vibrant Cyan Water Corridor Core
            Polyline innerPolyline = new Polyline();
            innerPolyline.setPoints(waterPolylinePoints);
            innerPolyline.getOutlinePaint().setColor(Color.parseColor("#00E5FF")); // Bright Cyan River Core
            innerPolyline.getOutlinePaint().setStrokeWidth(10.0f);
            innerPolyline.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
            innerPolyline.getOutlinePaint().setStrokeJoin(Paint.Join.ROUND);
            binding.mapView.getOverlays().add(innerPolyline);

            // Auto-zoom map to fit the entire river channel with margin
            if (maxLat > minLat && maxLon > minLon) {
                BoundingBox box = new BoundingBox(maxLat + 0.12, maxLon + 0.12, minLat - 0.12, minLon - 0.12);
                binding.mapView.post(() -> binding.mapView.zoomToBoundingBox(box, true, 80));
            }
        }

        binding.mapView.invalidate();
    }

    private WaterwayEdge findEdge(String fromId, String toId) {
        for (WaterwayEdge edge : networkData.routes) {
            if (edge.connects(fromId, toId)) {
                return edge;
            }
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
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
