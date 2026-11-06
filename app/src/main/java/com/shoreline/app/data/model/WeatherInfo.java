package com.shoreline.app.data.model;

import java.util.Locale;

/**
 * SYLLABUS: Marine Weather Model - Derived from Open-Meteo API
 * Maps wind speeds and weather codes to Bangladesh Meteorological Department (BMD) river storm signals.
 */
public class WeatherInfo {
    private final double temperatureC;
    private final double windSpeedKmh;
    private final double windGustKmh;
    private final int weatherCode;
    private final int riverSignalNumber; // 1 to 4 river warning signal
    private final String riverStatusDescription;
    private final boolean isLaunchTravelSafe;

    public WeatherInfo(double temperatureC, double windSpeedKmh, double windGustKmh, int weatherCode) {
        this.temperatureC = temperatureC;
        this.windSpeedKmh = windSpeedKmh;
        this.windGustKmh = windGustKmh;
        this.weatherCode = weatherCode;

        // Derive Bangladesh Inland Waterways Danger Signals (BIWTA / BMD Standards):
        // Signal 1 (Cautionary): Wind 20-35 km/h. Small trawlers take caution.
        // Signal 2 (Warning): Wind 35-50 km/h. Launch departures monitored.
        // Signal 3 (Danger / Kalbaishakhi): Wind 50-70 km/h. Confluence crossing prohibited.
        // Signal 4 (Great Danger): Wind > 70 km/h. All river launch operations halted.
        boolean isBn = "bn".equals(Locale.getDefault().getLanguage());
        if (windGustKmh > 70 || windSpeedKmh > 60) {
            this.riverSignalNumber = 4;
            this.riverStatusDescription = isBn
                    ? "মহাবিপদ সংকেত ৪: সকল নৌযান চলাচল স্থগিত!"
                    : "Great Danger Signal 4: Launch operations suspended!";
            this.isLaunchTravelSafe = false;
        } else if (windGustKmh > 50 || windSpeedKmh > 40) {
            this.riverSignalNumber = 3;
            this.riverStatusDescription = isBn
                    ? "বিপদ সংকেত ৩: মেঘনা ও পদ্মায় তীব্র কালবৈশাখী ঝড়ের সম্ভাবনা!"
                    : "Danger Signal 3: Severe Kalbaishakhi squalls on Meghna/Padma!";
            this.isLaunchTravelSafe = false;
        } else if (windGustKmh > 35 || windSpeedKmh > 28) {
            this.riverSignalNumber = 2;
            this.riverStatusDescription = isBn
                    ? "হুঁশিয়ারি সংকেত ২: উত্তাল নদী; উপকূলীয় চ্যানেল দিয়ে চলুন।"
                    : "Warning Signal 2: Choppy waters; navigate inland channels.";
            this.isLaunchTravelSafe = true;
        } else {
            this.riverSignalNumber = 1;
            this.riverStatusDescription = isBn
                    ? "সতর্ক সংকেত ১: নৌপথ শান্ত ও নৌচলাচলের উপযোগী।"
                    : "Cautionary Signal 1: River weather normal & navigable.";
            this.isLaunchTravelSafe = true;
        }
    }

    public double getTemperatureC() { return temperatureC; }
    public double getWindSpeedKmh() { return windSpeedKmh; }
    public double getWindGustKmh() { return windGustKmh; }
    public int getWeatherCode() { return weatherCode; }
    public int getRiverSignalNumber() { return riverSignalNumber; }
    public String getRiverStatusDescription() { return riverStatusDescription; }
    public boolean isLaunchTravelSafe() { return isLaunchTravelSafe; }
}
