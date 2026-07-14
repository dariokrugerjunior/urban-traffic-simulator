package com.trafficsimulator.routing.application;

import com.trafficsimulator.routing.domain.model.RoadNetwork;

/** Closes or reopens a street so future routes detour around it (reacting to a closure event). */
public class SetStreetBlockedUseCase {
    private final RoadNetwork network;

    public SetStreetBlockedUseCase(RoadNetwork network) {
        this.network = network;
    }

    public void setBlocked(String streetId, boolean blocked) {
        network.setBlocked(streetId, blocked);
    }
}
