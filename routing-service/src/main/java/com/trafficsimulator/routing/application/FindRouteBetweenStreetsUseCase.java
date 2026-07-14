package com.trafficsimulator.routing.application;

import com.trafficsimulator.routing.domain.model.DijkstraPathFinder;
import com.trafficsimulator.routing.domain.model.RoadNetwork;
import com.trafficsimulator.routing.domain.model.Route;
import com.trafficsimulator.routing.domain.model.RouteStreet;

/**
 * Routes between two streets (the UI picks edges, not intersections). Resolution is kept
 * deliberately simple for the MVP: start at the origin street's exit node and end at the
 * destination street's entry node.
 */
public class FindRouteBetweenStreetsUseCase {
    private final RoadNetwork network;
    private final DijkstraPathFinder pathFinder;

    public FindRouteBetweenStreetsUseCase(RoadNetwork network, DijkstraPathFinder pathFinder) {
        this.network = network;
        this.pathFinder = pathFinder;
    }

    public Route find(String originStreetId, String destStreetId) {
        RouteStreet origin = network.street(originStreetId);
        RouteStreet dest = network.street(destStreetId);
        if (origin == null || dest == null) {
            return Route.notFound();
        }
        return pathFinder.findShortest(network, origin.to(), dest.from());
    }
}
