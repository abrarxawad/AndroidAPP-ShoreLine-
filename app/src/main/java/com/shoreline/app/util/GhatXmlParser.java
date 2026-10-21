package com.shoreline.app.util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.shoreline.app.R;
import com.shoreline.app.data.model.GhatNode;
import com.shoreline.app.data.model.WaterwayEdge;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SYLLABUS: XML Manual Parsing - XmlPullParser implementation
 * Manually parses river network XML file including fine-grained river channel waypoint geometry.
 */
public class GhatXmlParser {

    private static final String TAG = "GhatXmlParser";

    public static class RiverNetworkData {
        public final Map<String, GhatNode> ghatMap = new LinkedHashMap<>();
        public final List<WaterwayEdge> routes = new ArrayList<>();
    }

    public static RiverNetworkData parseRiverNetwork(Context context) {
        RiverNetworkData data = new RiverNetworkData();
        XmlResourceParser parser = context.getResources().getXml(R.xml.river_network);
        WaterwayEdge currentEdge = null;

        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if ("ghat".equals(tagName)) {
                        String id = parser.getAttributeValue(null, "id");
                        String name = parser.getAttributeValue(null, "name");
                        String river = parser.getAttributeValue(null, "river");
                        double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                        double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                        String division = parser.getAttributeValue(null, "division");

                        GhatNode node = new GhatNode(id, name, river, lat, lon, division);
                        data.ghatMap.put(id, node);

                    } else if ("route".equals(tagName)) {
                        String id = parser.getAttributeValue(null, "id");
                        String from = parser.getAttributeValue(null, "from");
                        String to = parser.getAttributeValue(null, "to");
                        double distanceKm = Double.parseDouble(parser.getAttributeValue(null, "distanceKm"));
                        int baseTimeMinutes = Integer.parseInt(parser.getAttributeValue(null, "baseTimeMinutes"));
                        String hazard = parser.getAttributeValue(null, "hazard");
                        String channelType = parser.getAttributeValue(null, "channelType");

                        currentEdge = new WaterwayEdge(id, from, to, distanceKm, baseTimeMinutes, hazard, channelType);
                        data.routes.add(currentEdge);

                    } else if ("waypoint".equals(tagName) && currentEdge != null) {
                        double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                        double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                        currentEdge.addWaypoint(lat, lon);
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    String tagName = parser.getName();
                    if ("route".equals(tagName)) {
                        currentEdge = null;
                    }
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException | NumberFormatException e) {
            Log.e(TAG, "Error manually parsing river network XML", e);
        } finally {
            parser.close();
        }

        return data;
    }
}
