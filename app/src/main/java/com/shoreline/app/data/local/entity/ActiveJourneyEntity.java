package com.shoreline.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * SYLLABUS: Room Entity - Active River Journey for Family Tracker
 * Stores voyage metadata, 6-character share code, and latest GPS telemetry.
 */
@Entity(tableName = "active_journeys")
public class ActiveJourneyEntity {

    @PrimaryKey
    @NonNull
    private String journeyCode; // 6-character unique share code (e.g., "PADMA7")

    private String passengerId;
    private String passengerName;
    private String passengerPhone;
    private String emergencyContactPhone;
    private String vesselName;

    private String originGhatId;
    private String originGhatName;
    private String destGhatId;
    private String destGhatName;

    private long startedAt;
    private long lastUpdatedAt;
    private String status; // "ACTIVE", "ARRIVED", "STOPPED"

    private double currentLatitude;
    private double currentLongitude;
    private double currentSpeedKmh;
    private double totalDistanceKm;
    private String nearestGhatName;
    private boolean isSynced;

    public ActiveJourneyEntity(@NonNull String journeyCode, String passengerId, String passengerName,
                               String passengerPhone, String emergencyContactPhone, String vesselName,
                               String originGhatId, String originGhatName, String destGhatId,
                               String destGhatName, long startedAt, long lastUpdatedAt, String status,
                               double currentLatitude, double currentLongitude, double currentSpeedKmh,
                               double totalDistanceKm, String nearestGhatName, boolean isSynced) {
        this.journeyCode = journeyCode;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
        this.emergencyContactPhone = emergencyContactPhone;
        this.vesselName = vesselName;
        this.originGhatId = originGhatId;
        this.originGhatName = originGhatName;
        this.destGhatId = destGhatId;
        this.destGhatName = destGhatName;
        this.startedAt = startedAt;
        this.lastUpdatedAt = lastUpdatedAt;
        this.status = status;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.currentSpeedKmh = currentSpeedKmh;
        this.totalDistanceKm = totalDistanceKm;
        this.nearestGhatName = nearestGhatName;
        this.isSynced = isSynced;
    }

    @NonNull
    public String getJourneyCode() {
        return journeyCode;
    }

    public void setJourneyCode(@NonNull String journeyCode) {
        this.journeyCode = journeyCode;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getVesselName() {
        return vesselName;
    }

    public void setVesselName(String vesselName) {
        this.vesselName = vesselName;
    }

    public String getOriginGhatId() {
        return originGhatId;
    }

    public void setOriginGhatId(String originGhatId) {
        this.originGhatId = originGhatId;
    }

    public String getOriginGhatName() {
        return originGhatName;
    }

    public void setOriginGhatName(String originGhatName) {
        this.originGhatName = originGhatName;
    }

    public String getDestGhatId() {
        return destGhatId;
    }

    public void setDestGhatId(String destGhatId) {
        this.destGhatId = destGhatId;
    }

    public String getDestGhatName() {
        return destGhatName;
    }

    public void setDestGhatName(String destGhatName) {
        this.destGhatName = destGhatName;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(long lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public double getCurrentSpeedKmh() {
        return currentSpeedKmh;
    }

    public void setCurrentSpeedKmh(double currentSpeedKmh) {
        this.currentSpeedKmh = currentSpeedKmh;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public String getNearestGhatName() {
        return nearestGhatName;
    }

    public void setNearestGhatName(String nearestGhatName) {
        this.nearestGhatName = nearestGhatName;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
