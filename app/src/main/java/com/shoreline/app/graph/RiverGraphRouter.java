package com.shoreline.app.graph;

import com.shoreline.app.data.model.GhatNode;
import com.shoreline.app.data.model.NodeWeather;
import com.shoreline.app.data.model.WaterwayEdge;
import com.shoreline.app.data.model.WeatherInfo;
import com.shoreline.app.util.GhatXmlParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * SYLLABUS: Graph Algorithms - Dijkstra Pathfinding on River Network with Weather Edge Weights
 * Dynamically computes safe waterway navigation paths, routing around stormy river confluences.
 */
public class RiverGraphRouter {

    public static class RouteResult {
        public final List<GhatNode> path;
        public final List<WaterwayEdge> edges;
        public final double totalDistanceKm;
        public final double totalWeightedCost;
        public final int totalTimeMinutes;
        public final boolean isStormRerouted;
        public final String weatherNotice;
        public final String algorithmUsed;
        public final List<GhatNode> baselinePath;
        public final double baselineDistanceKm;
        public final String routeComparisonExplanation;
        public final int worstRiskLevel;
        public final String worstNodeName;
        public final String recommendation; // "GO", "BE_CAREFUL", "DELAY_TRIP"
        public final String recommendationTitle;
        public final String recommendationDetails;

        public RouteResult(List<GhatNode> path, List<WaterwayEdge> edges, double totalDistanceKm,
                           int totalTimeMinutes, boolean isStormRerouted, String weatherNotice, String algorithmUsed) {
            this(path, edges, totalDistanceKm, totalDistanceKm, totalTimeMinutes, isStormRerouted,
                    weatherNotice, algorithmUsed, path, totalDistanceKm,
                    "Standard waterway route selected.", NodeWeather.RISK_LOW,
                    (path != null && !path.isEmpty()) ? path.get(0).getName() : "Origin",
                    "GO", "Safe to Travel (যাত্রা নিরাপদ)", "All waterway points report safe operating conditions.");
        }

        public RouteResult(List<GhatNode> path, List<WaterwayEdge> edges, double totalDistanceKm,
                           double totalWeightedCost, int totalTimeMinutes, boolean isStormRerouted,
                           String weatherNotice, String algorithmUsed, List<GhatNode> baselinePath,
                           double baselineDistanceKm, String routeComparisonExplanation,
                           int worstRiskLevel, String worstNodeName, String recommendation,
                           String recommendationTitle, String recommendationDetails) {
            this.path = path;
            this.edges = edges;
            this.totalDistanceKm = totalDistanceKm;
            this.totalWeightedCost = totalWeightedCost;
            this.totalTimeMinutes = totalTimeMinutes;
            this.isStormRerouted = isStormRerouted;
            this.weatherNotice = weatherNotice;
            this.algorithmUsed = algorithmUsed;
            this.baselinePath = baselinePath;
            this.baselineDistanceKm = baselineDistanceKm;
            this.routeComparisonExplanation = routeComparisonExplanation;
            this.worstRiskLevel = worstRiskLevel;
            this.worstNodeName = worstNodeName;
            this.recommendation = recommendation;
            this.recommendationTitle = recommendationTitle;
            this.recommendationDetails = recommendationDetails;
        }
    }

    private final Map<String, GhatNode> ghatMap;
    private final List<WaterwayEdge> edgeList;

    public RiverGraphRouter(GhatXmlParser.RiverNetworkData data) {
        this.ghatMap = data.ghatMap;
        this.edgeList = data.routes;
    }

    public Map<String, GhatNode> getGhatMap() {
        return ghatMap;
    }

    /**
     * SYLLABUS: Dynamic Weather Edge-Weight Calculation with Node-Level Multipliers
     *
     * FORMULA: edgeWeight = baseDistanceKm * Math.max(multiplier(fromNode), multiplier(toNode))
     *
     * RATIONALE FOR Math.max():
     * A river vessel traversing an edge from port A to port B must pass through the worst water
     * condition on that reach. If one end is experiencing severe 60 km/h gusts at a stormy river mouth
     * (e.g. Chandpur or Meghna Estuary), the whole segment is hazardous and cannot be safely traversed.
     * Taking the maximum guarantees that dangerous channels receive a heavy cost penalty, forcing
     * Dijkstra's algorithm to seek sheltered bypass corridors (e.g. Mehendiganj northern canal).
     */
    public double calculateEffectiveWeight(WaterwayEdge edge, Map<String, NodeWeather> weatherMap) {
        double baseDistance = edge.getDistanceKm();
        if (weatherMap == null || weatherMap.isEmpty()) return baseDistance;

        NodeWeather fromWeather = weatherMap.get(edge.getFromGhatId());
        NodeWeather toWeather = weatherMap.get(edge.getToGhatId());

        double fromMult = (fromWeather != null) ? fromWeather.getRiskMultiplier() : 1.0;
        double toMult = (toWeather != null) ? toWeather.getRiskMultiplier() : 1.0;

        // Apply hazard level bonus penalty if river channel is inherently an open bay/estuary
        double hazardFactor = 1.0;
        if (fromMult > 1.0 || toMult > 1.0) {
            if ("HIGH_WIND_ZONE".equals(edge.getHazardLevel()) || "COASTAL_SURGE".equals(edge.getHazardLevel())) {
                hazardFactor = 1.25;
            } else if ("ESTUARY_CROSSING".equals(edge.getChannelType()) || "OPEN_BAY_ESTUARY".equals(edge.getChannelType())) {
                hazardFactor = 1.2;
            }
        }

        double edgeMultiplier = Math.max(fromMult, toMult) * hazardFactor;
        return baseDistance * Math.max(1.0, edgeMultiplier);
    }

    /**
     * Backward-compatible legacy method for WeatherInfo signal evaluation
     */
    private double calculateEffectiveWeight(WaterwayEdge edge, WeatherInfo weather) {
        double baseDistance = edge.getDistanceKm();
        if (weather == null) return baseDistance;

        double riskMultiplier = 1.0;
        int signal = weather.getRiverSignalNumber();

        if (signal >= 3) {
            if ("HIGH_WIND_ZONE".equals(edge.getHazardLevel()) || "COASTAL_SURGE".equals(edge.getHazardLevel())) {
                riskMultiplier = 5.0;
            } else if ("ESTUARY_CROSSING".equals(edge.getChannelType()) || "OPEN_BAY_ESTUARY".equals(edge.getChannelType())) {
                riskMultiplier = 4.0;
            }
        } else if (signal == 2) {
            if ("HIGH_WIND_ZONE".equals(edge.getHazardLevel())) {
                riskMultiplier = 1.8;
            }
        }
        return baseDistance * riskMultiplier;
    }

    /**
     * Finds the safe river route by executing Dijkstra on the weather-adjusted waterway graph,
     * comparing against the baseline (unweighted) route, and computing departure safety recommendations.
     */
    public RouteResult findRouteWithNodeWeather(String startId, String targetId,
                                                Map<String, NodeWeather> weatherMap,
                                                int vesselSpeedKmh) {
        if (!ghatMap.containsKey(startId) || !ghatMap.containsKey(targetId)) return null;

        // 1. Compute Baseline Route (Pure distance, zero weather penalty)
        InternalPathResult baseline = runDijkstraCore(startId, targetId, null);
        if (baseline == null) return null;

        // 2. Compute Weather-Weighted Route
        InternalPathResult weatherRoute = runDijkstraCore(startId, targetId, weatherMap);
        if (weatherRoute == null) {
            weatherRoute = baseline; // Fallback to baseline if graph disconnected under extreme weights
        }

        // 3. Analyze Route Differences
        boolean isRerouted = false;
        double extraKm = weatherRoute.totalDistanceKm - baseline.totalDistanceKm;
        List<String> bypassedHazardNodes = new ArrayList<>();

        if (weatherRoute.path.size() != baseline.path.size() || extraKm > 0.5) {
            isRerouted = true;
            // Identify which nodes on baseline had risk and were avoided
            for (GhatNode bn : baseline.path) {
                if (!weatherRoute.path.contains(bn)) {
                    NodeWeather nw = (weatherMap != null) ? weatherMap.get(bn.getId()) : null;
                    if (nw != null && nw.getRiskLevel() > NodeWeather.RISK_LOW) {
                        bypassedHazardNodes.add(bn.getName());
                    }
                }
            }
        }

        // 4. Generate Plain-Language Explanation
        String routeComparisonExplanation;
        if (isRerouted && !bypassedHazardNodes.isEmpty()) {
            routeComparisonExplanation = String.format(Locale.US,
                    "Safer route is +%.1f km longer but avoids high wind/gusts at %s.",
                    extraKm, String.join(", ", bypassedHazardNodes));
        } else if (isRerouted) {
            routeComparisonExplanation = String.format(Locale.US,
                    "Algorithm selected a sheltered inland corridor (+%.1f km) for vessel safety.", extraKm);
        } else {
            routeComparisonExplanation = "Standard waterway route is optimal and maintains safe weather thresholds.";
        }

        // 5. Evaluate Worst Risk on Chosen Route and Generate Recommendation
        int worstRisk = NodeWeather.RISK_LOW;
        String worstNodeName = weatherRoute.path.get(0).getName();
        String worstNodeMetric = "";

        for (GhatNode node : weatherRoute.path) {
            NodeWeather nw = (weatherMap != null) ? weatherMap.get(node.getId()) : null;
            if (nw != null && nw.getRiskLevel() > worstRisk) {
                worstRisk = nw.getRiskLevel();
                worstNodeName = node.getName();
                worstNodeMetric = String.format(Locale.US, "Gust: %.1f km/h, Rain: %.1f mm/h", nw.getGustKmh(), nw.getRainMm());
            }
        }

        String recommendation;
        String recommendationTitle;
        String recommendationDetails;

        if (worstRisk == NodeWeather.RISK_HIGH) {
            recommendation = "DELAY_TRIP";
            recommendationTitle = "Delay Your Trip / যাত্রা স্থগিত রাখুন";
            recommendationDetails = String.format(Locale.US,
                    "Severe weather (Risk Level: HIGH) at %s (%s). High capsizing hazard on open water. Delay voyage until conditions calm.",
                    worstNodeName, worstNodeMetric);
        } else if (worstRisk == NodeWeather.RISK_MEDIUM) {
            recommendation = "BE_CAREFUL";
            recommendationTitle = "Be Careful / সতর্কতার সাথে চলুন";
            recommendationDetails = String.format(Locale.US,
                    "Moderate chop and wind gusts (Risk Level: MEDIUM) at %s. Vessels should navigate cautiously.",
                    worstNodeName);
        } else {
            recommendation = "GO";
            recommendationTitle = "Safe to Travel / যাত্রা নিরাপদ";
            recommendationDetails = "All route checkpoints report calm river waters and favorable sailing conditions.";
        }

        // Recalculate travel time using user's chosen vessel speed
        int speed = Math.max(10, vesselSpeedKmh);
        int totalMinutes = (int) Math.round((weatherRoute.totalDistanceKm / (double) speed) * 60.0);

        String notice = isRerouted
                ? "⚠️ WEATHER REROUTE ACTIVE: Navigation adjusted through sheltered channels."
                : "✓ Standard river corridor selected.";

        return new RouteResult(
                weatherRoute.path,
                weatherRoute.edges,
                weatherRoute.totalDistanceKm,
                weatherRoute.totalCost,
                totalMinutes,
                isRerouted,
                notice,
                "Dijkstra",
                baseline.path,
                baseline.totalDistanceKm,
                routeComparisonExplanation,
                worstRisk,
                worstNodeName,
                recommendation,
                recommendationTitle,
                recommendationDetails
        );
    }

    /**
     * Legacy Dijkstra call using WeatherInfo
     */
    public RouteResult findRouteDijkstra(String startId, String targetId, WeatherInfo weather) {
        if (!ghatMap.containsKey(startId) || !ghatMap.containsKey(targetId)) return null;

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        Map<String, WaterwayEdge> previousEdges = new HashMap<>();

        for (String id : ghatMap.keySet()) {
            distances.put(id, Double.MAX_VALUE);
        }
        distances.put(startId, 0.0);

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a.distance));
        pq.add(new NodeDistance(startId, 0.0));
        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String u = current.nodeId;
            if (u.equals(targetId)) break;
            if (visited.contains(u)) continue;
            visited.add(u);

            for (WaterwayEdge edge : getAdjacentEdges(u)) {
                String v = edge.getOtherGhatId(u);
                if (v == null || visited.contains(v)) continue;

                double weight = calculateEffectiveWeight(edge, weather);
                double newDist = distances.get(u) + weight;

                if (newDist < distances.get(v)) {
                    distances.put(v, newDist);
                    previousNodes.put(v, u);
                    previousEdges.put(v, edge);
                    pq.add(new NodeDistance(v, newDist));
                }
            }
        }

        return reconstructLegacyPath(startId, targetId, previousNodes, previousEdges, weather, "Dijkstra");
    }

    /**
     * Internal core Dijkstra solver supporting arbitrary node weather weighting
     */
    private InternalPathResult runDijkstraCore(String startId, String targetId,
                                               Map<String, NodeWeather> weatherMap) {
        Map<String, Double> costs = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        Map<String, WaterwayEdge> previousEdges = new HashMap<>();

        for (String id : ghatMap.keySet()) {
            costs.put(id, Double.MAX_VALUE);
        }
        costs.put(startId, 0.0);

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a.distance));
        pq.add(new NodeDistance(startId, 0.0));
        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String u = current.nodeId;
            if (u.equals(targetId)) break;
            if (visited.contains(u)) continue;
            visited.add(u);

            for (WaterwayEdge edge : getAdjacentEdges(u)) {
                String v = edge.getOtherGhatId(u);
                if (v == null || visited.contains(v)) continue;

                double weight = calculateEffectiveWeight(edge, weatherMap);
                double newCost = costs.get(u) + weight;

                if (newCost < costs.get(v)) {
                    costs.put(v, newCost);
                    previousNodes.put(v, u);
                    previousEdges.put(v, edge);
                    pq.add(new NodeDistance(v, newCost));
                }
            }
        }

        if (!previousNodes.containsKey(targetId) && !startId.equals(targetId)) {
            return null; // Target unreachable
        }

        List<GhatNode> path = new ArrayList<>();
        List<WaterwayEdge> edges = new ArrayList<>();
        String curr = targetId;

        while (curr != null && !curr.equals(startId)) {
            path.add(ghatMap.get(curr));
            WaterwayEdge edge = previousEdges.get(curr);
            if (edge != null) edges.add(edge);
            curr = previousNodes.get(curr);
        }
        path.add(ghatMap.get(startId));
        Collections.reverse(path);
        Collections.reverse(edges);

        double physicalDistance = 0.0;
        int baseTime = 0;
        for (WaterwayEdge e : edges) {
            physicalDistance += e.getDistanceKm();
            baseTime += e.getBaseTimeMinutes();
        }

        return new InternalPathResult(path, edges, physicalDistance, costs.get(targetId), baseTime);
    }

    private static class InternalPathResult {
        final List<GhatNode> path;
        final List<WaterwayEdge> edges;
        final double totalDistanceKm;
        final double totalCost;
        final int baseTimeMinutes;

        InternalPathResult(List<GhatNode> path, List<WaterwayEdge> edges,
                           double totalDistanceKm, double totalCost, int baseTimeMinutes) {
            this.path = path;
            this.edges = edges;
            this.totalDistanceKm = totalDistanceKm;
            this.totalCost = totalCost;
            this.baseTimeMinutes = baseTimeMinutes;
        }
    }

    private List<WaterwayEdge> getAdjacentEdges(String ghatId) {
        List<WaterwayEdge> list = new ArrayList<>();
        for (WaterwayEdge edge : edgeList) {
            if (edge.getFromGhatId().equals(ghatId) || edge.getToGhatId().equals(ghatId)) {
                list.add(edge);
            }
        }
        return list;
    }

    private RouteResult reconstructLegacyPath(String startId, String targetId,
                                              Map<String, String> previousNodes,
                                              Map<String, WaterwayEdge> previousEdges,
                                              WeatherInfo weather, String algoName) {
        if (!previousNodes.containsKey(targetId) && !startId.equals(targetId)) {
            return null;
        }

        List<GhatNode> path = new ArrayList<>();
        List<WaterwayEdge> edges = new ArrayList<>();
        String current = targetId;

        while (current != null && !current.equals(startId)) {
            path.add(ghatMap.get(current));
            WaterwayEdge edge = previousEdges.get(current);
            if (edge != null) edges.add(edge);
            current = previousNodes.get(current);
        }
        path.add(ghatMap.get(startId));
        Collections.reverse(path);
        Collections.reverse(edges);

        double totalDistance = 0.0;
        int totalMinutes = 0;
        boolean hasRerouted = false;

        for (WaterwayEdge edge : edges) {
            totalDistance += edge.getDistanceKm();
            totalMinutes += edge.getBaseTimeMinutes();
            if (weather != null && weather.getRiverSignalNumber() >= 2 &&
                    ("HIGH_WIND_ZONE".equals(edge.getHazardLevel()) || "COASTAL_SURGE".equals(edge.getHazardLevel()))) {
                hasRerouted = true;
            }
        }

        String notice = (weather != null && weather.getRiverSignalNumber() >= 3)
                ? "⚠️ DANGER SIGNAL ACTIVE: Algorithm rerouted through sheltered inland waterways."
                : "✓ River weather normal. All channels open for navigation.";

        return new RouteResult(path, edges, totalDistance, totalMinutes, hasRerouted, notice, algoName);
    }

    private static class NodeDistance {
        final String nodeId;
        final double distance;
        NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }
}
