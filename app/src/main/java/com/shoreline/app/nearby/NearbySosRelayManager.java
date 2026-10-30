package com.shoreline.app.nearby;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.shoreline.app.data.local.ShoreLineDatabase;
import com.shoreline.app.data.local.dao.SosDao;
import com.shoreline.app.data.local.entity.SosSignalEntity;
import com.shoreline.app.receiver.SosAlertReceiver;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * SYLLABUS: Google Nearby Connections API - P2P_CLUSTER Offline Distress Mesh Relay
 *
 * Implements a decentralized, tower-independent ad-hoc mesh relay across Bangladesh riverways:
 * 1. P2P_CLUSTER Strategy: Enables multi-peer many-to-many wireless topology over Bluetooth & Wi-Fi Direct.
 * 2. Simultaneous Advertising & Discovery: Every boat in the fleet functions as both a node and a relay.
 * 3. Multi-Hop Forwarding with TTL (Hop Count limit = 3): Distributes emergency beacons across water reaches.
 * 4. Deduplication: Queries Room SQLite before re-transmitting to prevent broadcast storm saturation.
 */
public class NearbySosRelayManager {

    private static final String TAG = "NearbySosRelayManager";
    public static final String SERVICE_ID = "com.shoreline.app.OFFLINE_SOS";
    public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    public static final int MAX_HOPS = 3;

    public interface NearbyMeshListener {
        void onPeerConnected(String endpointId, String endpointName);
        void onPeerDisconnected(String endpointId);
        void onDistressBeaconReceived(SosSignalEntity beacon, int hopCount);
        void onBeaconBroadcastComplete(String sosId, int peersReached);
    }

    private static volatile NearbySosRelayManager INSTANCE;
    private final Context context;
    private final ConnectionsClient connectionsClient;
    private final ShoreLineDatabase database;
    private final SosDao sosDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final String localDeviceId;
    private final String localEndpointName;

    private boolean isAdvertising = false;
    private boolean isDiscovering = false;

    private final Map<String, String> connectedEndpoints = new HashMap<>(); // endpointId -> endpointName
    private final Set<String> processedSosIds = Collections.synchronizedSet(new HashSet<>());
    private final List<NearbyMeshListener> listeners = new ArrayList<>();

    private NearbySosRelayManager(Context context) {
        this.context = context.getApplicationContext();
        this.connectionsClient = Nearby.getConnectionsClient(this.context);
        this.database = ShoreLineDatabase.getInstance(this.context);
        this.sosDao = database.sosDao();

        this.localDeviceId = UUID.randomUUID().toString().substring(0, 8);
        this.localEndpointName = "Boat_" + localDeviceId;
    }

    public static NearbySosRelayManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NearbySosRelayManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NearbySosRelayManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Activates simultaneous advertising and discovery to join the river P2P mesh network.
     */
    public void startMesh() {
        startAdvertising();
        startDiscovery();
    }

    public void stopMesh() {
        try {
            if (isAdvertising) {
                connectionsClient.stopAdvertising();
                isAdvertising = false;
            }
            if (isDiscovering) {
                connectionsClient.stopDiscovery();
                isDiscovering = false;
            }
            connectionsClient.stopAllEndpoints();
            connectedEndpoints.clear();
            Log.d(TAG, "Offline SOS P2P Mesh stopped.");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping mesh: " + e.getMessage());
        }
    }

    private void startAdvertising() {
        if (isAdvertising) return;
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder()
                .setStrategy(STRATEGY)
                .build();

        connectionsClient.startAdvertising(
                localEndpointName,
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
        ).addOnSuccessListener(unused -> {
            isAdvertising = true;
            Log.d(TAG, "Nearby Advertising started successfully as " + localEndpointName);
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Nearby Advertising failed to start: " + e.getMessage());
        });
    }

    private void startDiscovery() {
        if (isDiscovering) return;
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder()
                .setStrategy(STRATEGY)
                .build();

        connectionsClient.startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                discoveryOptions
        ).addOnSuccessListener(unused -> {
            isDiscovering = true;
            Log.d(TAG, "Nearby Discovery started successfully.");
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Nearby Discovery failed to start: " + e.getMessage());
        });
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
            Log.d(TAG, "Found nearby river vessel: " + info.getEndpointName() + " (" + endpointId + ")");
            // Auto-request connection to join the cluster
            connectionsClient.requestConnection(localEndpointName, endpointId, connectionLifecycleCallback)
                    .addOnFailureListener(e -> Log.d(TAG, "Connection request rejected/failed: " + e.getMessage()));
        }

        @Override
        public void onEndpointLost(@NonNull String endpointId) {
            Log.d(TAG, "Lost nearby vessel signal: " + endpointId);
            connectedEndpoints.remove(endpointId);
            for (NearbyMeshListener listener : listeners) {
                listener.onPeerDisconnected(endpointId);
            }
        }
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            Log.d(TAG, "Connection initiated with: " + connectionInfo.getEndpointName());
            // Automatically accept peer connections in emergency mesh
            connectionsClient.acceptConnection(endpointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution resolution) {
            if (resolution.getStatus().isSuccess()) {
                connectedEndpoints.put(endpointId, "River_Vessel_" + endpointId.substring(0, Math.min(4, endpointId.length())));
                Log.d(TAG, "Connected to vessel: " + endpointId + " (Total active peers: " + connectedEndpoints.size() + ")");
                for (NearbyMeshListener listener : listeners) {
                    listener.onPeerConnected(endpointId, connectedEndpoints.get(endpointId));
                }
            } else {
                Log.d(TAG, "Connection to " + endpointId + " failed with status: " + resolution.getStatus());
            }
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            Log.d(TAG, "Disconnected from vessel: " + endpointId);
            connectedEndpoints.remove(endpointId);
            for (NearbyMeshListener listener : listeners) {
                listener.onPeerDisconnected(endpointId);
            }
        }
    };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            if (payload.getType() == Payload.Type.BYTES && payload.asBytes() != null) {
                String jsonString = new String(payload.asBytes(), StandardCharsets.UTF_8);
                handleIncomingDistressPayload(jsonString, endpointId);
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {}
    };

    /**
     * SYLLABUS: Multi-Hop Mesh Relay Logic & Loop Deduplication
     */
    private void handleIncomingDistressPayload(String jsonString, String fromEndpointId) {
        try {
            JSONObject json = new JSONObject(jsonString);
            if (!"SOS_BEACON".equals(json.optString("type"))) return;

            String sosId = json.getString("sosId");
            String senderName = json.optString("senderName", "Passenger in Distress");
            String senderPhone = json.optString("senderPhone", "");
            String vesselName = json.optString("vesselName", "River Vessel");
            double lat = json.optDouble("latitude", 23.2321);
            double lon = json.optDouble("longitude", 90.6473);
            long timestamp = json.optLong("timestamp", System.currentTimeMillis());
            int hopCount = json.optInt("hopCount", 0);
            String originDeviceId = json.optString("originDeviceId", "UNKNOWN");
            String emergencyType = json.optString("emergencyType", "CAPSIZING_RISK");

            // 1. DEDUPLICATION: Drop if seen recently in memory or Room database
            if (processedSosIds.contains(sosId) || localDeviceId.equals(originDeviceId)) {
                Log.d(TAG, "Dropped duplicate SOS beacon " + sosId + " from " + fromEndpointId);
                return;
            }
            processedSosIds.add(sosId);

            ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
                SosSignalEntity existing = sosDao.getSosById(sosId);
                if (existing != null) {
                    Log.d(TAG, "Beacon " + sosId + " already cached in Room SQLite; discarding loop.");
                    return;
                }

                // 2. Persist to Room SQLite
                SosSignalEntity entity = new SosSignalEntity(
                        sosId, senderName, senderPhone, lat, lon, timestamp,
                        hopCount, originDeviceId, "ACTIVE", true, false
                );
                sosDao.insertSos(entity);

                // 3. Fire In-App Custom Broadcast to trigger Morse alarm
                Intent alertIntent = new Intent(SosAlertReceiver.ACTION_SOS_BEACON_DETECTED);
                alertIntent.setPackage(context.getPackageName());
                alertIntent.putExtra(SosAlertReceiver.EXTRA_SOS_ID, sosId);
                alertIntent.putExtra(SosAlertReceiver.EXTRA_SENDER_NAME, senderName);
                alertIntent.putExtra(SosAlertReceiver.EXTRA_VESSEL_NAME, vesselName);
                alertIntent.putExtra(SosAlertReceiver.EXTRA_LATITUDE, lat);
                alertIntent.putExtra(SosAlertReceiver.EXTRA_LONGITUDE, lon);
                alertIntent.putExtra(SosAlertReceiver.EXTRA_EMERGENCY_TYPE, emergencyType);
                alertIntent.putExtra(SosAlertReceiver.EXTRA_HOP_COUNT, hopCount);
                context.sendBroadcast(alertIntent);

                // 4. Notify UI Listeners
                mainHandler.post(() -> {
                    for (NearbyMeshListener listener : listeners) {
                        listener.onDistressBeaconReceived(entity, hopCount);
                    }
                });

                // 5. Multi-Hop Forwarding: Relay to all other connected peers if hopCount < MAX_HOPS
                if (hopCount < MAX_HOPS) {
                    relayDistressBeacon(json, fromEndpointId, hopCount + 1);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse incoming SOS payload: " + e.getMessage());
        }
    }

    private void relayDistressBeacon(JSONObject originalJson, String excludeEndpointId, int nextHop) {
        try {
            originalJson.put("hopCount", nextHop);
            byte[] bytes = originalJson.toString().getBytes(StandardCharsets.UTF_8);
            Payload relayPayload = Payload.fromBytes(bytes);

            List<String> targetEndpoints = new ArrayList<>();
            for (String endpoint : connectedEndpoints.keySet()) {
                if (!endpoint.equals(excludeEndpointId)) {
                    targetEndpoints.add(endpoint);
                }
            }

            if (!targetEndpoints.isEmpty()) {
                connectionsClient.sendPayload(targetEndpoints, relayPayload)
                        .addOnSuccessListener(unused -> Log.d(TAG, "Successfully relayed SOS to " + targetEndpoints.size() + " neighboring vessels (Hop " + nextHop + ")"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Relay forwarding error: " + e.getMessage());
        }
    }

    /**
     * SYLLABUS: Transmit High-Priority Offline SOS Beacon across Nearby Connections Mesh
     */
    public void broadcastDistressBeacon(
            String senderName,
            String senderPhone,
            String vesselName,
            double latitude,
            double longitude,
            String emergencyType
    ) {
        String sosId = "SOS-" + System.currentTimeMillis() + "-" + localDeviceId;
        long now = System.currentTimeMillis();

        processedSosIds.add(sosId);

        // 1. Save to Room database as originated beacon
        SosSignalEntity selfBeacon = new SosSignalEntity(
                sosId, senderName, senderPhone, latitude, longitude,
                now, 0, localDeviceId, "ACTIVE", false, false
        );

        ShoreLineDatabase.databaseWriteExecutor.execute(() -> {
            sosDao.insertSos(selfBeacon);
        });

        // 2. Build JSON Payload
        try {
            JSONObject json = new JSONObject();
            json.put("type", "SOS_BEACON");
            json.put("sosId", sosId);
            json.put("senderName", senderName);
            json.put("senderPhone", senderPhone);
            json.put("vesselName", vesselName);
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            json.put("timestamp", now);
            json.put("hopCount", 0);
            json.put("originDeviceId", localDeviceId);
            json.put("emergencyType", emergencyType);

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            Payload payload = Payload.fromBytes(bytes);

            if (!connectedEndpoints.isEmpty()) {
                List<String> targets = new ArrayList<>(connectedEndpoints.keySet());
                connectionsClient.sendPayload(targets, payload)
                        .addOnSuccessListener(unused -> {
                            Log.d(TAG, "Broadcast SOS sent to " + targets.size() + " peer vessels.");
                            notifyBroadcastComplete(sosId, targets.size());
                        });
            } else {
                Log.d(TAG, "No direct peer endpoints connected currently; beacon cached in Room for next contact.");
                notifyBroadcastComplete(sosId, 0);
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to broadcast distress beacon: " + e.getMessage());
        }
    }

    private void notifyBroadcastComplete(String sosId, int peersCount) {
        mainHandler.post(() -> {
            for (NearbyMeshListener listener : listeners) {
                listener.onBeaconBroadcastComplete(sosId, peersCount);
            }
        });
    }

    public void registerListener(NearbyMeshListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(NearbyMeshListener listener) {
        listeners.remove(listener);
    }

    public int getConnectedPeerCount() {
        return connectedEndpoints.size();
    }

    public List<String> getConnectedPeerNames() {
        return new ArrayList<>(connectedEndpoints.values());
    }
}
