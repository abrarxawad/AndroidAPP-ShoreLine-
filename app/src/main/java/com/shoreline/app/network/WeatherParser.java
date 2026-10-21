package com.shoreline.app.network;

import androidx.annotation.Nullable;

import com.shoreline.app.data.model.GhatNode;
import com.shoreline.app.data.model.NodeWeather;
import com.shoreline.app.data.model.WeatherInfo;
import com.shoreline.app.util.RiskScorer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * SYLLABUS: JSON Manual Parsing - org.json.JSONObject & JSONArray
 * Manually parses Open-Meteo multi-location batch API responses without external reflection-based libraries.
 * Handles both single-location JSON objects and multi-coordinate JSON arrays.
 */
public class WeatherParser {

    private static final SimpleDateFormat ISO_FORMAT;
    static {
        ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:00", Locale.US);
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Dhaka"));
    }

    /**
     * Legacy single-point parser for Chandpur live weather
     */
    public static WeatherInfo parseCurrentWeather(String jsonString) throws JSONException {
        JSONObject root = new JSONObject(jsonString);
        JSONObject current = root.getJSONObject("current");

        double temperature = current.optDouble("temperature_2m", 28.5);
        double windSpeed = current.optDouble("wind_speed_10m", 22.0);
        double windGusts = current.optDouble("wind_gusts_10m", 32.0);
        int weatherCode = current.optInt("weather_code", 0);

        return new WeatherInfo(temperature, windSpeed, windGusts, weatherCode);
    }

    /**
     * Parses multi-coordinate batch Open-Meteo forecast JSON response.
     * The response array elements map 1:1 in exact index sequence to the requested nodes list.
     *
     * @param jsonString Raw response string from Open-Meteo
     * @param targetNodes Ordered list of ghat nodes submitted in the query
     * @param targetTimes Ordered list of target ISO times (yyyy-MM-dd'T'HH:00) per node
     * @param marineWaveMap Optional map of nodeId -> waveHeightM from Marine API
     * @return Map of nodeId -> NodeWeather
     */
    public static Map<String, NodeWeather> parseBatchNodeWeather(
            String jsonString,
            List<GhatNode> targetNodes,
            List<String> targetTimes,
            @Nullable Map<String, Double> marineWaveMap) throws JSONException {

        Map<String, NodeWeather> resultMap = new HashMap<>();
        long now = System.currentTimeMillis();

        JSONArray locationsArray;
        String trimmed = jsonString.trim();
        if (trimmed.startsWith("[")) {
            locationsArray = new JSONArray(trimmed);
        } else {
            // Single location returned as a flat object
            locationsArray = new JSONArray();
            locationsArray.put(new JSONObject(trimmed));
        }

        int count = Math.min(locationsArray.length(), targetNodes.size());
        for (int i = 0; i < count; i++) {
            JSONObject locationObj = locationsArray.getJSONObject(i);
            GhatNode node = targetNodes.get(i);
            String targetTime = (i < targetTimes.size()) ? targetTimes.get(i) : getCurrentDhakaHourString();

            if (!locationObj.has("hourly")) continue;

            JSONObject hourly = locationObj.getJSONObject("hourly");
            JSONArray timeArray = hourly.getJSONArray("time");
            JSONArray tempArray = hourly.optJSONArray("temperature_2m");
            JSONArray windArray = hourly.optJSONArray("wind_speed_10m");
            JSONArray gustArray = hourly.optJSONArray("wind_gusts_10m");
            JSONArray rainArray = hourly.optJSONArray("precipitation");
            JSONArray codeArray = hourly.optJSONArray("weather_code");

            // Find closest matching hour in the forecast array
            int closestIndex = findClosestHourIndex(timeArray, targetTime);

            double temp = (tempArray != null && closestIndex < tempArray.length()) ? tempArray.optDouble(closestIndex, 28.0) : 28.0;
            double wind = (windArray != null && closestIndex < windArray.length()) ? windArray.optDouble(closestIndex, 20.0) : 20.0;
            double gust = (gustArray != null && closestIndex < gustArray.length()) ? gustArray.optDouble(closestIndex, 30.0) : (wind * 1.35);
            double rain = (rainArray != null && closestIndex < rainArray.length()) ? rainArray.optDouble(closestIndex, 0.0) : 0.0;
            int code = (codeArray != null && closestIndex < codeArray.length()) ? codeArray.optInt(closestIndex, 1) : 1;

            Double waveHeight = (marineWaveMap != null) ? marineWaveMap.get(node.getId()) : null;

            int riskLevel = RiskScorer.evaluateRiskLevel(wind, gust, rain, waveHeight, code);
            double riskMultiplier = RiskScorer.getMultiplierForRiskLevel(riskLevel);

            String resolvedTime = (closestIndex < timeArray.length()) ? timeArray.optString(closestIndex, targetTime) : targetTime;

            NodeWeather nodeWeather = new NodeWeather(
                    node.getId(),
                    node.getName(),
                    resolvedTime,
                    temp,
                    wind,
                    gust,
                    rain,
                    code,
                    waveHeight,
                    now,
                    riskLevel,
                    riskMultiplier
            );
            resultMap.put(node.getId(), nodeWeather);
        }

        return resultMap;
    }

    /**
     * Searches the ISO time array for the entry closest to the target time string.
     */
    private static int findClosestHourIndex(JSONArray timeArray, String targetTime) {
        if (timeArray == null || timeArray.length() == 0) return 0;

        // Try exact match first
        for (int i = 0; i < timeArray.length(); i++) {
            if (targetTime.equals(timeArray.optString(i))) {
                return i;
            }
        }

        // Try matching hour prefix (e.g. "2026-10-22T14")
        String prefix = targetTime.length() >= 13 ? targetTime.substring(0, 13) : targetTime;
        for (int i = 0; i < timeArray.length(); i++) {
            String entry = timeArray.optString(i);
            if (entry.startsWith(prefix)) {
                return i;
            }
        }

        return 0; // Default to first available forecast hour if outside range
    }

    public static String getCurrentDhakaHourString() {
        synchronized (ISO_FORMAT) {
            return ISO_FORMAT.format(new Date());
        }
    }

    public static String formatDhakaHour(Date date) {
        synchronized (ISO_FORMAT) {
            return ISO_FORMAT.format(date);
        }
    }
}
