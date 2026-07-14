package com.trafficsimulator.trafficstate.domain.model;

import java.util.Objects;

/**
 * A street in the road network: an edge of the city graph connecting two
 * intersections. In the macroscopic model a street is treated as a "pipe"
 * with a fixed hourly capacity through which a current volume of vehicles flows.
 *
 * <p>The congestion of a street is derived from {@code currentVolume / hourlyCapacity}.
 * Volume is intentionally <em>not</em> capped at capacity: an oversaturated street
 * (ratio &gt; 1.0) is a valid, and meaningful, {@link CongestionLevel#JAMMED} state.
 *
 * <p>This is a pure domain entity: it holds no framework annotations
 * (no {@code @Entity}, no Jackson, no Spring). Persistence and serialization are
 * the responsibility of infrastructure adapters that map to/from this type.
 *
 * <p>Identity is defined by {@link #id()}; two streets are equal iff they share
 * the same id.
 */
public final class Street {

    private final String id;
    private final String name;
    private final String fromIntersectionId;
    private final String toIntersectionId;
    private final int hourlyCapacity;

    private int currentVolume;
    private int trafficLightCount;
    private double greenRatio = 1.0;
    private boolean oneway;
    private boolean source;
    private boolean blocked;

    /**
     * Creates a street with an initial volume.
     *
     * @param id                 unique identity of the street; must not be blank
     * @param name               human-readable name; must not be blank
     * @param fromIntersectionId origin intersection id; must not be blank
     * @param toIntersectionId   destination intersection id; must not be blank
     * @param hourlyCapacity     maximum vehicles per hour; must be strictly positive
     * @param currentVolume      initial active vehicles; must not be negative
     * @throws IllegalArgumentException if any invariant is violated
     */
    public Street(String id,
                  String name,
                  String fromIntersectionId,
                  String toIntersectionId,
                  int hourlyCapacity,
                  int currentVolume) {
        this.id = requireNonBlank(id, "id");
        this.name = requireNonBlank(name, "name");
        this.fromIntersectionId = requireNonBlank(fromIntersectionId, "fromIntersectionId");
        this.toIntersectionId = requireNonBlank(toIntersectionId, "toIntersectionId");
        if (hourlyCapacity <= 0) {
            throw new IllegalArgumentException("hourlyCapacity must be strictly positive, but was: " + hourlyCapacity);
        }
        if (currentVolume < 0) {
            throw new IllegalArgumentException("currentVolume must not be negative, but was: " + currentVolume);
        }
        this.hourlyCapacity = hourlyCapacity;
        this.currentVolume = currentVolume;
    }

    /**
     * Creates an empty street (no traffic yet).
     */
    public Street(String id,
                  String name,
                  String fromIntersectionId,
                  String toIntersectionId,
                  int hourlyCapacity) {
        this(id, name, fromIntersectionId, toIntersectionId, hourlyCapacity, 0);
    }

    /**
     * Rehydration constructor for persistence adapters: restores the full state,
     * including traffic-light count and green ratio.
     *
     * @throws IllegalArgumentException if any invariant is violated
     */
    public Street(String id,
                  String name,
                  String fromIntersectionId,
                  String toIntersectionId,
                  int hourlyCapacity,
                  int currentVolume,
                  int trafficLightCount,
                  double greenRatio) {
        this(id, name, fromIntersectionId, toIntersectionId, hourlyCapacity, currentVolume);
        if (trafficLightCount < 0) {
            throw new IllegalArgumentException("trafficLightCount must not be negative, but was: " + trafficLightCount);
        }
        if (greenRatio <= 0 || greenRatio > 1) {
            throw new IllegalArgumentException("greenRatio must be in (0, 1], but was: " + greenRatio);
        }
        this.trafficLightCount = trafficLightCount;
        this.greenRatio = greenRatio;
    }

    /**
     * Adds a traffic light to this street, reducing its effective throughput.
     * Each light applies the given green-time fraction, compounding.
     *
     * @param greenRatio green-time / cycle-time fraction; must be in (0, 1]
     * @throws IllegalArgumentException if greenRatio is outside (0, 1]
     */
    public void addTrafficLight(double greenRatio) {
        if (greenRatio <= 0 || greenRatio > 1) {
            throw new IllegalArgumentException("greenRatio must be in (0, 1], but was: " + greenRatio);
        }
        this.greenRatio = greenRatio;
        this.trafficLightCount++;
    }

    /**
     * @return the throughput after traffic-light delays, always at least 1.
     */
    public int effectiveCapacity() {
        double effective = hourlyCapacity * Math.pow(greenRatio, trafficLightCount);
        return Math.max(1, (int) Math.floor(effective));
    }

    public int trafficLightCount() {
        return trafficLightCount;
    }

    public double greenRatio() {
        return greenRatio;
    }

    /** Whether the street is one-way (canonical direction: from → to). */
    public boolean oneway() {
        return oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    /** Whether the street continuously generates traffic (a simulation source). */
    public boolean source() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    /** Whether the street is closed: no traffic may enter it (it drains and stays empty). */
    public boolean blocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    /**
     * Adds vehicles to the current volume (e.g. reacting to an injected traffic flow).
     *
     * @param vehicles number of vehicles to add; must not be negative
     * @throws IllegalArgumentException if {@code vehicles} is negative
     */
    public void injectFlow(int vehicles) {
        if (vehicles < 0) {
            throw new IllegalArgumentException("Injected flow must not be negative, but was: " + vehicles);
        }
        this.currentVolume += vehicles;
    }

    /**
     * Removes vehicles from the current volume, never dropping below zero
     * (e.g. reacting to traffic draining away or being rerouted).
     *
     * @param vehicles number of vehicles to remove; must not be negative
     * @throws IllegalArgumentException if {@code vehicles} is negative
     */
    public void releaseFlow(int vehicles) {
        if (vehicles < 0) {
            throw new IllegalArgumentException("Released flow must not be negative, but was: " + vehicles);
        }
        this.currentVolume = Math.max(0, this.currentVolume - vehicles);
    }

    /**
     * Replaces the current volume with an absolute value.
     *
     * @param volume the new active-vehicle count; must not be negative
     * @throws IllegalArgumentException if {@code volume} is negative
     */
    public void updateVolume(int volume) {
        if (volume < 0) {
            throw new IllegalArgumentException("Volume must not be negative, but was: " + volume);
        }
        this.currentVolume = volume;
    }

    /**
     * @return the volume / effective-capacity ratio; may exceed 1.0 when oversaturated.
     */
    public double congestionRatio() {
        return (double) currentVolume / effectiveCapacity();
    }

    /**
     * @return the current {@link CongestionLevel} derived from the ratio.
     */
    public CongestionLevel congestionLevel() {
        return CongestionLevel.fromRatio(congestionRatio());
    }

    /**
     * @return {@code true} if the street is currently {@link CongestionLevel#JAMMED}.
     */
    public boolean isJammed() {
        return congestionLevel() == CongestionLevel.JAMMED;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String fromIntersectionId() {
        return fromIntersectionId;
    }

    public String toIntersectionId() {
        return toIntersectionId;
    }

    public int hourlyCapacity() {
        return hourlyCapacity;
    }

    public int currentVolume() {
        return currentVolume;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Street street)) {
            return false;
        }
        return id.equals(street.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Street{id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", from='" + fromIntersectionId + '\'' +
                ", to='" + toIntersectionId + '\'' +
                ", hourlyCapacity=" + hourlyCapacity +
                ", currentVolume=" + currentVolume +
                ", congestionLevel=" + congestionLevel() +
                '}';
    }
}
