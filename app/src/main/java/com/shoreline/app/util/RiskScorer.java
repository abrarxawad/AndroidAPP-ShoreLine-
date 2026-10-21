package com.shoreline.app.util;

import androidx.annotation.Nullable;

import com.shoreline.app.data.model.NodeWeather;

/**
 * SYLLABUS: Graph Risk Scoring & Cost Multipliers
 *
 * NOTE: The threshold values defined here represent this academic project's
 * heuristic navigation assumptions modeled after BIWTA vessel draft safety guidelines.
 * They are not official Bangladesh Meteorological Department (BMD) warning criteria.
 * Official BMD inland river warning signals and port authority orders take statutory precedence.
 */
public final class RiskScorer {

    private RiskScorer() {} // Utility class

    // --- Gust Thresholds (km/h) ---
    // Calm/Light: < 35 km/h (Inland launches operate normally)
    // Moderate: 35.0 - 50.0 km/h (Choppy river surface; smaller single-deck vessels must exercise caution)
    // Severe: > 50.0 km/h (Kalbaishakhi squall; high capsizing risk on wide river confluences)
    public static final double GUST_THRESHOLD_MEDIUM_KMH = 35.0;
    public static final double GUST_THRESHOLD_HIGH_KMH = 50.0;

    // --- Sustained Wind Thresholds (km/h) ---
    public static final double WIND_THRESHOLD_MEDIUM_KMH = 28.0;
    public static final double WIND_THRESHOLD_HIGH_KMH = 45.0;

    // --- Precipitation Thresholds (mm/h) ---
    // Light rain: < 2.0 mm/h
    // Moderate rain: 2.0 - 10.0 mm/h (Impairs visibility across wide confluences like Chandpur)
    // Torrential rain: > 10.0 mm/h (Severe blinding downpours, navigation radar required)
    public static final double RAIN_THRESHOLD_MEDIUM_MM = 2.0;
    public static final double RAIN_THRESHOLD_HIGH_MM = 10.0;

    // --- Wave Height Thresholds (meters) (applicable to coastal estuaries / Bay of Bengal) ---
    // Smooth/Slight: < 0.8 m
    // Moderate: 0.8 m - 1.5 m (Challenging for shallow-draft delta launches)
    // Rough/Dangerous: > 1.5 m (Risk of taking water over low freeboard)
    public static final double WAVE_THRESHOLD_MEDIUM_M = 0.8;
    public static final double WAVE_THRESHOLD_HIGH_M = 1.5;

    // --- Risk Multipliers for Dijkstra Graph Weighting ---
    // Multiplier MUST always be >= 1.0 (never below 1.0) to ensure distance metric preservation
    // and mathematical admissibility if A* heuristic is utilized.
    public static final double MULTIPLIER_LOW_RISK = 1.0;     // No penalty on safe waterways
    public static final double MULTIPLIER_MEDIUM_RISK = 1.8;  // 80% virtual distance penalty
    public static final double MULTIPLIER_HIGH_RISK = 4.0;    // 300% penalty forces avoidance of hazard

    /**
     * Evaluates weather metrics against thresholds and assigns a discrete Risk Level.
     *
     * @param windKmh Sustained 10m wind speed in km/h
     * @param gustKmh Peak 10m wind gust in km/h
     * @param rainMm Precipitation rate in mm/h
     * @param waveHeightM Significant wave height in meters (nullable for inland nodes)
     * @param weatherCode WMO standard weather interpretation code
     * @return NodeWeather.RISK_LOW (0), RISK_MEDIUM (1), or RISK_HIGH (2)
     */
    public static int evaluateRiskLevel(double windKmh, double gustKmh, double rainMm,
                                        @Nullable Double waveHeightM, int weatherCode) {
        // High Risk Conditions (Any single severe factor escalates node to High Risk)
        if (gustKmh >= GUST_THRESHOLD_HIGH_KMH || windKmh >= WIND_THRESHOLD_HIGH_KMH) {
            return NodeWeather.RISK_HIGH;
        }
        if (rainMm >= RAIN_THRESHOLD_HIGH_MM) {
            return NodeWeather.RISK_HIGH;
        }
        if (waveHeightM != null && waveHeightM >= WAVE_THRESHOLD_HIGH_M) {
            return NodeWeather.RISK_HIGH;
        }
        // Severe WMO codes: 95 (Thunderstorm), 96/99 (Severe thunderstorm with hail), 82 (Violent rain showers)
        if (weatherCode == 95 || weatherCode == 96 || weatherCode == 99 || weatherCode == 82 || weatherCode == 65) {
            return NodeWeather.RISK_HIGH;
        }

        // Medium Risk Conditions
        if (gustKmh >= GUST_THRESHOLD_MEDIUM_KMH || windKmh >= WIND_THRESHOLD_MEDIUM_KMH) {
            return NodeWeather.RISK_MEDIUM;
        }
        if (rainMm >= RAIN_THRESHOLD_MEDIUM_MM) {
            return NodeWeather.RISK_MEDIUM;
        }
        if (waveHeightM != null && waveHeightM >= WAVE_THRESHOLD_MEDIUM_M) {
            return NodeWeather.RISK_MEDIUM;
        }
        // Moderate WMO codes: 51-55 (Drizzle), 61-63 (Rain), 80-81 (Showers), 45/48 (Dense fog)
        if (weatherCode == 45 || weatherCode == 48 || (weatherCode >= 51 && weatherCode <= 63) || weatherCode == 80 || weatherCode == 81) {
            return NodeWeather.RISK_MEDIUM;
        }

        // Safe / Low Risk
        return NodeWeather.RISK_LOW;
    }

    /**
     * Converts a discrete Risk Level into a mathematical edge-weight multiplier >= 1.0.
     */
    public static double getMultiplierForRiskLevel(int riskLevel) {
        switch (riskLevel) {
            case NodeWeather.RISK_HIGH:
                return MULTIPLIER_HIGH_RISK;
            case NodeWeather.RISK_MEDIUM:
                return MULTIPLIER_MEDIUM_RISK;
            case NodeWeather.RISK_LOW:
            default:
                return MULTIPLIER_LOW_RISK;
        }
    }
}
