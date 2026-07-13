package com.trafficsimulator.routing.domain.model;

/**
 * A directed edge of the road network. Its routing {@link #weight()} is the base
 * weight scaled by a penalty factor that grows when the street becomes congested.
 */
public final class RouteStreet {
    private final String id;
    private final String name;
    private final String from;
    private final String to;
    private final double baseWeight;
    private double penaltyFactor;

    public RouteStreet(String id, String name, String from, String to, double baseWeight) {
        if (baseWeight <= 0) {
            throw new IllegalArgumentException("baseWeight must be positive, but was: " + baseWeight);
        }
        this.id = id;
        this.name = name;
        this.from = from;
        this.to = to;
        this.baseWeight = baseWeight;
        this.penaltyFactor = 1.0;
    }

    public void applyPenalty(double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("penalty factor must be positive, but was: " + factor);
        }
        this.penaltyFactor = factor;
    }

    public double weight() {
        return baseWeight * penaltyFactor;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String from() { return from; }
    public String to() { return to; }
    public double baseWeight() { return baseWeight; }
    public double penaltyFactor() { return penaltyFactor; }
}
