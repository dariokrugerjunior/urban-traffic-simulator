package com.trafficsimulator.routing.application;

import com.trafficsimulator.routing.domain.model.RoadNetwork;

/** Restores a street's normal routing weight after it clears up (no longer congested). */
public class ClearStreetUseCase {
    private final RoadNetwork network;

    public ClearStreetUseCase(RoadNetwork network) {
        this.network = network;
    }

    public void clear(String streetId) {
        network.penalize(streetId, 1.0);
    }
}
