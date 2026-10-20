package com.shoreline.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * SYLLABUS: Offline Queue Pattern (isSynced flag)
 * Demonstrates local-first queuing of reports when offline in mid-river.
 */
@Entity(tableName = "overload_reports")
public class OverloadReportEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "report_id")
    private String reportId;

    @ColumnInfo(name = "vessel_id")
    private String vesselId;

    @ColumnInfo(name = "vessel_number")
    private String vesselNumber;

    @ColumnInfo(name = "crowd_level")
    private String crowdLevel; // "LOW", "MODERATE", "CRITICAL_OVERLOAD"

    @ColumnInfo(name = "passenger_count_estimate")
    private int estimatedPassengerCount;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "photo_base64")
    private String photoBase64;

    @ColumnInfo(name = "reporter_user_id")
    private String reporterUserId;

    @ColumnInfo(name = "is_anonymous")
    private boolean isAnonymous;

    // SYLLABUS: Pending sync flag pattern (Offline-First data synchronization)
    @ColumnInfo(name = "is_synced")
    private boolean isSynced;

    @ColumnInfo(name = "sync_timestamp")
    private long syncTimestamp;

    public OverloadReportEntity(@NonNull String reportId, String vesselId, String vesselNumber,
                                String crowdLevel, int estimatedPassengerCount, double latitude,
                                double longitude, long timestamp, String photoBase64,
                                String reporterUserId, boolean isAnonymous, boolean isSynced,
                                long syncTimestamp) {
        this.reportId = reportId;
        this.vesselId = vesselId;
        this.vesselNumber = vesselNumber;
        this.crowdLevel = crowdLevel;
        this.estimatedPassengerCount = estimatedPassengerCount;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.photoBase64 = photoBase64;
        this.reporterUserId = reporterUserId;
        this.isAnonymous = isAnonymous;
        this.isSynced = isSynced;
        this.syncTimestamp = syncTimestamp;
    }

    @NonNull
    public String getReportId() { return reportId; }
    public void setReportId(@NonNull String reportId) { this.reportId = reportId; }

    public String getVesselId() { return vesselId; }
    public void setVesselId(String vesselId) { this.vesselId = vesselId; }

    public String getVesselNumber() { return vesselNumber; }
    public void setVesselNumber(String vesselNumber) { this.vesselNumber = vesselNumber; }

    public String getCrowdLevel() { return crowdLevel; }
    public void setCrowdLevel(String crowdLevel) { this.crowdLevel = crowdLevel; }

    public int getEstimatedPassengerCount() { return estimatedPassengerCount; }
    public void setEstimatedPassengerCount(int count) { this.estimatedPassengerCount = count; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }

    public String getReporterUserId() { return reporterUserId; }
    public void setReporterUserId(String reporterUserId) { this.reporterUserId = reporterUserId; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }

    public long getSyncTimestamp() { return syncTimestamp; }
    public void setSyncTimestamp(long syncTimestamp) { this.syncTimestamp = syncTimestamp; }
}
