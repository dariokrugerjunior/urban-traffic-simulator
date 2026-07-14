package com.trafficsimulator.trafficstate.domain.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory road graph that propagates traffic each tick using a simple macroscopic model.
 * Pure domain: no framework, no I/O.
 *
 * <p>Each street is a directed edge (canonical direction from → to). Flow leaves a street at
 * its {@code to} node and spreads into the streets enterable from there, split by their free
 * capacity. Streets whose exit node has no onward street are <b>sinks</b> where flow leaves the
 * simulation. A small per-tick <b>decay</b> guarantees the fluid can never deadlock.
 */
public class SimulationNetwork {

    private final Map<String, Street> streets = new LinkedHashMap<>();
    /** node id → streets that can be entered starting from that node. */
    private final Map<String, List<Street>> enterableFrom = new HashMap<>();

    public void addStreet(Street street) {
        streets.put(street.id(), street);
        enterableFrom.computeIfAbsent(street.fromIntersectionId(), k -> new ArrayList<>()).add(street);
        if (!street.oneway()) {
            enterableFrom.computeIfAbsent(street.toIntersectionId(), k -> new ArrayList<>()).add(street);
        }
    }

    public Collection<Street> streets() {
        return streets.values();
    }

    public Street get(String id) {
        return streets.get(id);
    }

    public int size() {
        return streets.size();
    }

    /** Streets reachable when leaving {@code s} at its exit node, excluding {@code s} itself. */
    private List<Street> downstream(Street s) {
        List<Street> candidates = enterableFrom.getOrDefault(s.toIntersectionId(), List.of());
        List<Street> result = new ArrayList<>(candidates.size());
        for (Street d : candidates) {
            if (d != s) {
                result.add(d);
            }
        }
        return result;
    }

    /**
     * Runs one propagation tick (computed into buffers, then applied simultaneously so the
     * result is order-independent).
     *
     * @param outRate    fraction of a street's effective capacity that can leave per tick (0..1)
     * @param decay      fraction of volume that dissipates each tick (0..1) — keeps the model stable
     * @param sourceRate vehicles a source street injects into itself each tick
     */
    public void tick(double outRate, double decay, int sourceRate) {
        Map<String, Double> inflow = new HashMap<>();
        Map<String, Double> outflow = new HashMap<>();

        for (Street s : streets.values()) {
            double out = Math.min(s.currentVolume(), s.effectiveCapacity() * outRate);
            List<Street> ds = downstream(s);
            if (ds.isEmpty()) {
                outflow.put(s.id(), out); // sink: flow exits the network
                continue;
            }
            double totalFree = 0;
            for (Street d : ds) {
                totalFree += Math.max(0, d.effectiveCapacity() - d.currentVolume());
            }
            if (totalFree <= 0) {
                outflow.put(s.id(), 0.0); // backpressure: downstream full, hold (congestion builds)
                continue;
            }
            double actual = Math.min(out, totalFree);
            for (Street d : ds) {
                double free = Math.max(0, d.effectiveCapacity() - d.currentVolume());
                if (free > 0) {
                    inflow.merge(d.id(), actual * (free / totalFree), Double::sum);
                }
            }
            outflow.put(s.id(), actual);
        }

        for (Street s : streets.values()) {
            double volume = s.currentVolume()
                    - outflow.getOrDefault(s.id(), 0.0)
                    + inflow.getOrDefault(s.id(), 0.0);
            if (s.source()) {
                volume += sourceRate;
            }
            volume *= (1 - decay);
            s.updateVolume((int) Math.max(0, Math.round(volume)));
        }
    }
}
