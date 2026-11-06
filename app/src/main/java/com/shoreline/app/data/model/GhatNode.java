package com.shoreline.app.data.model;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * SYLLABUS: Graph Algorithm Node - River Ghat / Port
 * Supports dynamic locale-aware localization between English and Bangla.
 */
public class GhatNode {
    private final String id;
    private final String name;
    private final String river;
    private final double latitude;
    private final double longitude;
    private final String division;

    public GhatNode(String id, String name, String river, double latitude, double longitude, String division) {
        this.id = id;
        this.name = name;
        this.river = river;
        this.latitude = latitude;
        this.longitude = longitude;
        this.division = division;
    }

    public String getId() { return id; }

    public String getName() {
        if ("bn".equals(Locale.getDefault().getLanguage())) {
            return getBanglaName();
        }
        return name;
    }

    public String getRawEnglishName() { return name; }

    public String getRiver() {
        if ("bn".equals(Locale.getDefault().getLanguage())) {
            return getBanglaRiver();
        }
        return river;
    }

    public String getRawEnglishRiver() { return river; }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDivision() { return division; }

    public String getBanglaName() {
        if (id == null) return name;
        switch (id) {
            case "G01": return "ঢাকা (সদরঘাট)";
            case "G02": return "চাঁদপুর (মোলহেড ঘাট)";
            case "G03": return "বরিশাল (নদী বন্দর)";
            case "G04": return "ভোলা (ইলিশা ঘাট)";
            case "G05": return "পটুয়াখালী (লঞ্চ ঘাট)";
            case "G06": return "খুলনা (রূপসা ঘাট)";
            case "G07": return "মোংলা বন্দর (সুন্দরবন)";
            case "G08": return "হাতিয়া দ্বীপ (নলচিরা)";
            default: return name;
        }
    }

    public String getBanglaRiver() {
        if (id == null) return river;
        switch (id) {
            case "G01": return "বুড়িগঙ্গা";
            case "G02": return "পদ্মা-মেঘনা";
            case "G03": return "কীর্তনখোলা";
            case "G04": return "মেঘনা-তেঁতুলিয়া";
            case "G05": return "লৌহকাঠি";
            case "G06": return "রূপসা";
            case "G07": return "পশুর নদী";
            case "G08": return "উপকূলীয় মোহনা";
            default: return river;
        }
    }

    @NonNull
    @Override
    public String toString() {
        if ("bn".equals(Locale.getDefault().getLanguage())) {
            return getBanglaName() + " (" + getBanglaRiver() + ")";
        }
        return name + " (" + river + ")";
    }
}
