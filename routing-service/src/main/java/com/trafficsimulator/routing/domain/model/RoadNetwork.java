package com.trafficsimulator.routing.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory directed graph of the city's streets. */
public class RoadNetwork {
    private final Map<String, Intersection> intersections = new LinkedHashMap<>();
    /** Canonical (first-added) directed edge per id — used for state and street→node resolution. */
    private final Map<String, RouteStreet> streets = new LinkedHashMap<>();
    /** Every directed edge sharing an id (a two-way street has both directions). */
    private final Map<String, List<RouteStreet>> byId = new HashMap<>();
    private final Map<String, List<RouteStreet>> adjacency = new HashMap<>();

    public void addIntersection(Intersection node) {
        intersections.put(node.id(), node);
        adjacency.computeIfAbsent(node.id(), k -> new ArrayList<>());
    }

    public void addStreet(RouteStreet street) {
        streets.putIfAbsent(street.id(), street); // canonical = the first direction added
        byId.computeIfAbsent(street.id(), k -> new ArrayList<>()).add(street);
        adjacency.computeIfAbsent(street.from(), k -> new ArrayList<>()).add(street);
    }

    public List<RouteStreet> outgoing(String nodeId) {
        return adjacency.getOrDefault(nodeId, List.of());
    }

    /** Applies a penalty to every direction of the street (a two-way street penalizes both). */
    public void penalize(String streetId, double factor) {
        for (RouteStreet s : byId.getOrDefault(streetId, List.of())) {
            s.applyPenalty(factor);
        }
    }

    /** Closes or reopens a street (both directions) so the path finder detours. No-op if unknown. */
    public void setBlocked(String streetId, boolean blocked) {
        for (RouteStreet s : byId.getOrDefault(streetId, List.of())) {
            s.setBlocked(blocked);
        }
    }

    /** The canonical (from→to) street with this id, or {@code null} (used to resolve street→node). */
    public RouteStreet street(String streetId) {
        return streets.get(streetId);
    }

    public List<RouteStreet> streets() {
        return new ArrayList<>(streets.values());
    }

    public boolean hasNode(String id) {
        return intersections.containsKey(id);
    }
}
