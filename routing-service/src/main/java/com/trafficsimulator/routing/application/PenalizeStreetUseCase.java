package com.trafficsimulator.routing.application;

import com.trafficsimulator.routing.domain.model.RoadNetwork;

/** Penalizes a congested street's routing weight so future routes avoid it. */
public class PenalizeStreetUseCase {
    public static final double DEFAULT_PENALTY = 10.0;
    private final RoadNetwork network;

    public PenalizeStreetUseCase(RoadNetwork network) {
        this.network = network;
    }

    public void penalize(String streetId) {
        network.penalize(streetId, DEFAULT_PENALTY);
    }
}
