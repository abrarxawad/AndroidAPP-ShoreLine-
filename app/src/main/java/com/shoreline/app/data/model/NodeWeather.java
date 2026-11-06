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
        boolean isBn = "bn".equals(Locale.getDefault().getLanguage());
        switch (weatherCode) {
            case 0: return isBn ? "পরিষ্কার আকাশ" : "Clear Sky";
            case 1: return isBn ? "বেশিরভাগ পরিষ্কার" : "Mainly Clear";
            case 2: return isBn ? "আংশিক মেঘলা" : "Partly Cloudy";
            case 3: return isBn ? "মেঘলা" : "Overcast";
            case 45:
            case 48: return isBn ? "নদীতে কুয়াশা" : "River Fog / Mist";
            case 51:
            case 53:
            case 55: return isBn ? "গুঁড়ি গুঁড়ি বৃষ্টি" : "Light Drizzle";
            case 61: return isBn ? "হালকা বৃষ্টি" : "Slight Rain";
            case 63: return isBn ? "মাঝারি বৃষ্টি" : "Moderate Rain";
            case 65: return isBn ? "ভারী বর্ষণ" : "Heavy Downpour";
            case 80:
            case 81: return isBn ? "বৃষ্টির দমকা" : "Rain Showers";
            case 82: return isBn ? "তীব্র ঝড়বৃষ্টি" : "Violent Squall Showers";
            case 95: return isBn ? "বজ্রঝড় / কালবৈশাখী" : "Thunderstorm";
            case 96:
            case 99: return isBn ? "শিলাবৃষ্টিসহ তীব্র ঝড়" : "Severe Thunderstorm with Hail";
            default: return isBn ? "স্বাভাবিক আবহাওয়া" : "Seasonal Weather";
        }
    }

    /**
     * Maps risk level to user-friendly status badge text
     */
    public String getRiskBadgeText() {
        boolean isBn = "bn".equals(Locale.getDefault().getLanguage());
        switch (riskLevel) {
            case RISK_HIGH: return isBn ? "⛔ বিপদ" : "⛔ DANGER";
            case RISK_MEDIUM: return isBn ? "⚠️ সতর্কতা" : "⚠️ CAUTION";
            default: return isBn ? "✓ নিরাপদ" : "✓ SAFE";
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
