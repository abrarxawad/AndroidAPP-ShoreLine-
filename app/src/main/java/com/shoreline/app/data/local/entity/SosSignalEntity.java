package com.shoreline.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * SYLLABUS: Room Entity - Offline SOS Mesh Beacon
 * Stores distress signals for local deduplication and multi-hop relay.
 */
@Entity(tableName = "sos_signals")
public class SosSignalEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "sos_id")
    private String sosId;

    @ColumnInfo(name = "sender_name")
    private String senderName;

    @ColumnInfo(name = "sender_phone")
    private String senderPhone;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "hop_count")
    private int hopCount;

    @ColumnInfo(name = "origin_device_id")
    private String originDeviceId;

    @ColumnInfo(name = "status")
    private String status; // "ACTIVE", "ACKNOWLEDGED", "RESCUED"

    @ColumnInfo(name = "is_relayed")
    private boolean isRelayed;

    @ColumnInfo(name = "is_synced_to_cloud")
    private boolean isSyncedToCloud;

    public SosSignalEntity(@NonNull String sosId, String senderName, String senderPhone,
                           double latitude, double longitude, long timestamp,
                           int hopCount, String originDeviceId, String status,
                           boolean isRelayed, boolean isSyncedToCloud) {
        this.sosId = sosId;
        this.senderName = senderName;
        this.senderPhone = senderPhone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.hopCount = hopCount;
        this.originDeviceId = originDeviceId;
        this.status = status;
        this.isRelayed = isRelayed;
        this.isSyncedToCloud = isSyncedToCloud;
    }

    @NonNull
    public String getSosId() { return sosId; }
    public void setSosId(@NonNull String sosId) { this.sosId = sosId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getHopCount() { return hopCount; }
    public void setHopCount(int hopCount) { this.hopCount = hopCount; }

    public String getOriginDeviceId() { return originDeviceId; }
    public void setOriginDeviceId(String originDeviceId) { this.originDeviceId = originDeviceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isRelayed() { return isRelayed; }
    public void setRelayed(boolean relayed) { isRelayed = relayed; }

    public boolean isSyncedToCloud() { return isSyncedToCloud; }
    public void setSyncedToCloud(boolean syncedToCloud) { isSyncedToCloud = syncedToCloud; }
}
