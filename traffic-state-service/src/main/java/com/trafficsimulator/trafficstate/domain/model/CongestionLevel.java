package com.trafficsimulator.trafficstate.domain.model;

/**
 * Macroscopic congestion classification of a {@link Street}, derived from the
 * ratio between its current volume and its hourly capacity.
 *
 * <p>Classification thresholds (see PROJECT_CONTEXT):
 * <ul>
 *     <li>{@code ratio < 0.50} &rarr; {@link #FREE} (Green)</li>
 *     <li>{@code 0.50 <= ratio <= 0.80} &rarr; {@link #HEAVY} (Yellow)</li>
 *     <li>{@code ratio > 0.80} &rarr; {@link #JAMMED} (Red)</li>
 * </ul>
 *
 * <p>This is a pure domain type: it carries no framework annotations.
 */
public enum CongestionLevel {

    FREE("Green"),
    HEAVY("Yellow"),
    JAMMED("Red");

    private static final double HEAVY_THRESHOLD = 0.50;
    private static final double JAMMED_THRESHOLD = 0.80;

    private final String color;

    CongestionLevel(String color) {
        this.color = color;
    }

    /**
     * Classifies a volume/capacity ratio into the matching congestion level.
     *
     * @param ratio the fraction {@code currentVolume / hourlyCapacity}; must not be negative
     * @return the congestion level for the given ratio
     * @throws IllegalArgumentException if {@code ratio} is negative or not a finite number
     */
    public static CongestionLevel fromRatio(double ratio) {
        if (Double.isNaN(ratio) || Double.isInfinite(ratio)) {
            throw new IllegalArgumentException("Congestion ratio must be a finite number, but was: " + ratio);
        }
        if (ratio < 0) {
            throw new IllegalArgumentException("Congestion ratio must not be negative, but was: " + ratio);
        }
        if (ratio < HEAVY_THRESHOLD) {
            return FREE;
        }
        if (ratio <= JAMMED_THRESHOLD) {
            return HEAVY;
        }
        return JAMMED;
    }

    /**
     * @return the UI color associated with this level (Green, Yellow or Red).
     */
    public String color() {
        return color;
    }
}
