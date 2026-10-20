package com.shoreline.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * SYLLABUS: Room Entity - Vessel Registry Cache
 * Caches vessel details for 100% offline lookup mid-river.
 */
@Entity(tableName = "vessels", indices = {@Index(value = {"registration_number"}, unique = true)})
public class VesselEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "vessel_id")
    private String vesselId;

    @ColumnInfo(name = "registration_number")
    private String registrationNumber;

    @ColumnInfo(name = "vessel_name")
    private String vesselName;

    @ColumnInfo(name = "route_name")
    private String routeName;

    @ColumnInfo(name = "licensed_capacity")
    private int licensedCapacity;

    @ColumnInfo(name = "fitness_expiry")
    private String fitnessExpiryDate;

    @ColumnInfo(name = "community_rating")
    private float communityRating;

    @ColumnInfo(name = "total_reports")
    private int totalReports;

    @ColumnInfo(name = "is_flagged")
    private boolean isFlagged;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public VesselEntity(@NonNull String vesselId, String registrationNumber, String vesselName,
                        String routeName, int licensedCapacity, String fitnessExpiryDate,
                        float communityRating, int totalReports, boolean isFlagged, long cachedAt) {
        this.vesselId = vesselId;
        this.registrationNumber = registrationNumber;
        this.vesselName = vesselName;
        this.routeName = routeName;
        this.licensedCapacity = licensedCapacity;
        this.fitnessExpiryDate = fitnessExpiryDate;
        this.communityRating = communityRating;
        this.totalReports = totalReports;
        this.isFlagged = isFlagged;
        this.cachedAt = cachedAt;
    }

    @NonNull
    public String getVesselId() { return vesselId; }
    public void setVesselId(@NonNull String vesselId) { this.vesselId = vesselId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getVesselName() { return vesselName; }
    public void setVesselName(String vesselName) { this.vesselName = vesselName; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public int getLicensedCapacity() { return licensedCapacity; }
    public void setLicensedCapacity(int licensedCapacity) { this.licensedCapacity = licensedCapacity; }

    public String getFitnessExpiryDate() { return fitnessExpiryDate; }
    public void setFitnessExpiryDate(String fitnessExpiryDate) { this.fitnessExpiryDate = fitnessExpiryDate; }

    public float getCommunityRating() { return communityRating; }
    public void setCommunityRating(float communityRating) { this.communityRating = communityRating; }

    public int getTotalReports() { return totalReports; }
    public void setTotalReports(int totalReports) { this.totalReports = totalReports; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
}
