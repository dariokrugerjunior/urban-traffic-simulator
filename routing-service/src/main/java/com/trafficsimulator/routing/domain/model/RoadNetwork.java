package com.trafficsimulator.routing.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory directed graph of the city's streets. */
public class RoadNetwork {
    private final Map<String, Intersection> intersections = new LinkedHashMap<>();
    private final Map<String, RouteStreet> streets = new LinkedHashMap<>();
    private final Map<String, List<RouteStreet>> adjacency = new HashMap<>();

    public void addIntersection(Intersection node) {
        intersections.put(node.id(), node);
        adjacency.computeIfAbsent(node.id(), k -> new ArrayList<>());
    }

    public void addStreet(RouteStreet street) {
        streets.put(street.id(), street);
        adjacency.computeIfAbsent(street.from(), k -> new ArrayList<>()).add(street);
    }

    public List<RouteStreet> outgoing(String nodeId) {
        return adjacency.getOrDefault(nodeId, List.of());
    }

    public void penalize(String streetId, double factor) {
        RouteStreet s = streets.get(streetId);
        if (s != null) {
            s.applyPenalty(factor);
        }
    }

    public List<RouteStreet> streets() {
        return new ArrayList<>(streets.values());
    }

    public boolean hasNode(String id) {
        return intersections.containsKey(id);
    }
}
