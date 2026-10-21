package com.shoreline.app.data.model;

import androidx.annotation.NonNull;

/**
 * SYLLABUS: Graph Algorithm Node - River Ghat / Port
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
    public String getName() { return name; }
    public String getRiver() { return river; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDivision() { return division; }

    @NonNull
    @Override
    public String toString() {
        return name + " (" + river + ")";
    }
}
