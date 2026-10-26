package com.shoreline.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * SYLLABUS: Room Entity - GPS Breadcrumb Trail for River Journey Tracking
 * Stores high-frequency GPS coordinate history for polyline rendering and offline sync queue.
 */
@Entity(
        tableName = "journey_breadcrumbs",
        indices = {@Index("journeyCode")}
)
public class JourneyBreadcrumbEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String journeyCode;

    private double latitude;
    private double longitude;
    private double speedKmh;
    private long timestamp;
    private boolean isSynced;

    public JourneyBreadcrumbEntity(@NonNull String journeyCode, double latitude, double longitude,
                                   double speedKmh, long timestamp, boolean isSynced) {
        this.journeyCode = journeyCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.timestamp = timestamp;
        this.isSynced = isSynced;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getJourneyCode() {
        return journeyCode;
    }

    public void setJourneyCode(@NonNull String journeyCode) {
        this.journeyCode = journeyCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(double speedKmh) {
        this.speedKmh = speedKmh;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
