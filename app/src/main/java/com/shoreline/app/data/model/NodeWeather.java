package com.shoreline.app.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Locale;

/**
 * SYLLABUS: Graph Algorithm Node Weather Model
 * Strongly-typed representation of hourly meteorological & marine conditions
 * for a specific waterway port or intermediate river navigation waypoint.
 */
public class NodeWeather implements Serializable {

    public static final int RISK_LOW = 0;
    public static final int RISK_MEDIUM = 1;
    public static final int RISK_HIGH = 2;

    private final String nodeId;
    private final String nodeName;
    private final String forecastTime; // e.g. "2026-10-22T14:00"
    private final double tempC;
    private final double windKmh;
    private final double gustKmh;
    private final double rainMm;
    private final int weatherCode;
    @Nullable
    private final Double waveHeightM; // null for inland riverbeds
    private final long fetchedAt;
    private final int riskLevel;
    private final double riskMultiplier; // >= 1.0

    public NodeWeather(String nodeId, String nodeName, String forecastTime,
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
        this.riskMultiplier = Math.max(1.0, riskMultiplier);
    }

    public String getNodeId() { return nodeId; }
    public String getNodeName() { return nodeName; }
    public String getForecastTime() { return forecastTime; }
    public double getTempC() { return tempC; }
    public double getWindKmh() { return windKmh; }
    public double getGustKmh() { return gustKmh; }
    public double getRainMm() { return rainMm; }
    public int getWeatherCode() { return weatherCode; }
    @Nullable
    public Double getWaveHeightM() { return waveHeightM; }
    public long getFetchedAt() { return fetchedAt; }
    public int getRiskLevel() { return riskLevel; }
    public double getRiskMultiplier() { return riskMultiplier; }

    /**
     * Maps WMO weather code to clear waterway descriptive condition
     */
    public String getWeatherDescription() {
        switch (weatherCode) {
            case 0: return "Clear Sky (পরিষ্কার আকাশ)";
            case 1: return "Mainly Clear (বেশিরভাগ পরিষ্কার)";
            case 2: return "Partly Cloudy (আংশিক মেঘলা)";
            case 3: return "Overcast (মেঘলা)";
            case 45:
            case 48: return "River Fog / Mist (নদীতে কুয়াশা)";
            case 51:
            case 53:
            case 55: return "Light Drizzle (গুঁড়ি গুঁড়ি বৃষ্টি)";
            case 61: return "Slight Rain (হালকা বৃষ্টি)";
            case 63: return "Moderate Rain (মাঝারি বৃষ্টি)";
            case 65: return "Heavy Downpour (ভারী বর্ষণ)";
            case 80:
            case 81: return "Rain Showers (বৃষ্টির দমকা)";
            case 82: return "Violent Squall Showers (তীব্র ঝড়বৃষ্টি)";
            case 95: return "Thunderstorm (বজ্রঝড় / কালবৈশাখী)";
            case 96:
            case 99: return "Severe Thunderstorm with Hail (শিলাবৃষ্টিসহ তীব্র ঝড়)";
            default: return "Seasonal Weather (স্বাভাবিক আবহাওয়া)";
        }
    }

    /**
     * Maps risk level to user-friendly status badge text
     */
    public String getRiskBadgeText() {
        switch (riskLevel) {
            case RISK_HIGH: return "⛔ DANGER / বিপদ";
            case RISK_MEDIUM: return "⚠ CAUTION / সতর্কবার্তা";
            default: return "✓ SAFE / নিরাপদ";
        }
    }

    /**
     * Formats weather metrics summary in one readable line
     */
    public String getMetricsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "%.1f°C | Wind: %.1f km/h | Gusts: %.1f km/h", tempC, windKmh, gustKmh));
        if (rainMm > 0.0) {
            sb.append(String.format(Locale.US, " | Rain: %.1f mm/h", rainMm));
        }
        if (waveHeightM != null && waveHeightM > 0.1) {
            sb.append(String.format(Locale.US, " | Wave: %.1fm", waveHeightM));
        }
        return sb.toString();
    }

    /**
     * Returns how old the cached weather observation is
     */
    public String getCacheAgeLabel() {
        long ageMs = System.currentTimeMillis() - fetchedAt;
        long mins = ageMs / (60 * 1000L);
        if (mins <= 2) {
            return "Live (Just now)";
        } else if (mins < 60) {
            return mins + " mins ago";
        } else {
            long hours = mins / 60;
            return hours + " hr " + (mins % 60) + " mins ago";
        }
    }

    @NonNull
    @Override
    public String toString() {
        return nodeName + " (" + nodeId + "): " + getMetricsSummary() + " [" + getRiskBadgeText() + "]";
    }
}
