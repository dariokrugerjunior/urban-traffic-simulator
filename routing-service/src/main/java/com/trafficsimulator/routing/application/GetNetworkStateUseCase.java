package com.trafficsimulator.routing.application;

import com.trafficsimulator.routing.domain.model.RoadNetwork;
import com.trafficsimulator.routing.domain.model.RouteStreet;

import java.util.List;

public class GetNetworkStateUseCase {
    private final RoadNetwork network;

    public GetNetworkStateUseCase(RoadNetwork network) {
        this.network = network;
    }

    public List<RouteStreet> streets() {
        return network.streets();
    }
}
