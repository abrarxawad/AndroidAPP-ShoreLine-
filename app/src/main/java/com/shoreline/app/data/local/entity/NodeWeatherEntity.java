package com.shoreline.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;

/**
 * SYLLABUS: Room Entity - Cached Node Weather Persistence
 * Caches hourly forecast observations per river node to provide offline resilience
 * for waterway journey calculations without active network connection.
 */
@Entity(
        tableName = "node_weather",
        primaryKeys = {"nodeId", "forecastTime"},
        indices = {@Index(value = {"nodeId"}), @Index(value = {"fetchedAt"})}
)
public class NodeWeatherEntity {

    @NonNull
    private String nodeId;
    @NonNull
    private String nodeName;
    @NonNull
    private String forecastTime; // e.g. "2026-10-22T14:00"

    private double tempC;
    private double windKmh;
    private double gustKmh;
    private double rainMm;
    private int weatherCode;

    @Nullable
    private Double waveHeightM; // null for inland riverbeds

    private long fetchedAt;
    private int riskLevel;
    private double riskMultiplier;

    public NodeWeatherEntity(@NonNull String nodeId, @NonNull String nodeName, @NonNull String forecastTime,
                             double tempC, double windKmh, double gustKmh, double rainMm,
                             int weatherCode, @Nullable Double waveHeightM,
                             long fetchedAt, int riskLevel, double riskMultiplier) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.forecastTime = forecastTime;
        this.tempC = tempC;
        this.windKmh = windKmh;
        this.gustKmh = gustKmh;
        this.rainMm = rainMm;
        this.weatherCode = weatherCode;
        this.waveHeightM = waveHeightM;
        this.fetchedAt = fetchedAt;
        this.riskLevel = riskLevel;
        this.riskMultiplier = riskMultiplier;
    }

    @NonNull
    public String getNodeId() { return nodeId; }
    public void setNodeId(@NonNull String nodeId) { this.nodeId = nodeId; }

    @NonNull
    public String getNodeName() { return nodeName; }
    public void setNodeName(@NonNull String nodeName) { this.nodeName = nodeName; }

    @NonNull
    public String getForecastTime() { return forecastTime; }
    public void setForecastTime(@NonNull String forecastTime) { this.forecastTime = forecastTime; }

    public double getTempC() { return tempC; }
    public void setTempC(double tempC) { this.tempC = tempC; }

    public double getWindKmh() { return windKmh; }
    public void setWindKmh(double windKmh) { this.windKmh = windKmh; }

    public double getGustKmh() { return gustKmh; }
    public void setGustKmh(double gustKmh) { this.gustKmh = gustKmh; }

    public double getRainMm() { return rainMm; }
    public void setRainMm(double rainMm) { this.rainMm = rainMm; }

    public int getWeatherCode() { return weatherCode; }
    public void setWeatherCode(int weatherCode) { this.weatherCode = weatherCode; }

    @Nullable
    public Double getWaveHeightM() { return waveHeightM; }
    public void setWaveHeightM(@Nullable Double waveHeightM) { this.waveHeightM = waveHeightM; }

    public long getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(long fetchedAt) { this.fetchedAt = fetchedAt; }

    public int getRiskLevel() { return riskLevel; }
    public void setRiskLevel(int riskLevel) { this.riskLevel = riskLevel; }

    public double getRiskMultiplier() { return riskMultiplier; }
    public void setRiskMultiplier(double riskMultiplier) { this.riskMultiplier = riskMultiplier; }
}
