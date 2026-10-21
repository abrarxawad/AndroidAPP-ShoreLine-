package com.shoreline.app.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * SYLLABUS: Graph Algorithm Edge - River Channel Segment with True River Waterway Curves
 */
public class WaterwayEdge {
    private final String id;
    private final String fromGhatId;
    private final String toGhatId;
    private final double distanceKm;
    private final int baseTimeMinutes;
    private final String hazardLevel;
    private final String channelType;
    private final List<double[]> riverWaypoints = new ArrayList<>();

    public WaterwayEdge(String id, String fromGhatId, String toGhatId, double distanceKm,
                        int baseTimeMinutes, String hazardLevel, String channelType) {
        this.id = id;
        this.fromGhatId = fromGhatId;
        this.toGhatId = toGhatId;
        this.distanceKm = distanceKm;
        this.baseTimeMinutes = baseTimeMinutes;
        this.hazardLevel = hazardLevel;
        this.channelType = channelType;
    }

    public String getId() { return id; }
    public String getFromGhatId() { return fromGhatId; }
    public String getToGhatId() { return toGhatId; }
    public double getDistanceKm() { return distanceKm; }
    public int getBaseTimeMinutes() { return baseTimeMinutes; }
    public String getHazardLevel() { return hazardLevel; }
    public String getChannelType() { return channelType; }

    public void addWaypoint(double lat, double lon) {
        riverWaypoints.add(new double[]{lat, lon});
    }

    public List<double[]> getRiverWaypoints() {
        return riverWaypoints;
    }

    public boolean connects(String ghatA, String ghatB) {
        return (fromGhatId.equals(ghatA) && toGhatId.equals(ghatB)) ||
               (fromGhatId.equals(ghatB) && toGhatId.equals(ghatA));
    }

    public String getOtherGhatId(String currentGhatId) {
        if (fromGhatId.equals(currentGhatId)) return toGhatId;
        if (toGhatId.equals(currentGhatId)) return fromGhatId;
        return null;
    }
}
