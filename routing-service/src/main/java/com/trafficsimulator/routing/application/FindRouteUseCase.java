package com.trafficsimulator.routing.application;

import com.trafficsimulator.routing.domain.model.DijkstraPathFinder;
import com.trafficsimulator.routing.domain.model.RoadNetwork;
import com.trafficsimulator.routing.domain.model.Route;

public class FindRouteUseCase {
    private final RoadNetwork network;
    private final DijkstraPathFinder pathFinder;

    public FindRouteUseCase(RoadNetwork network, DijkstraPathFinder pathFinder) {
        this.network = network;
        this.pathFinder = pathFinder;
    }

    public Route find(String start, String end) {
        return pathFinder.findShortest(network, start, end);
    }
}
