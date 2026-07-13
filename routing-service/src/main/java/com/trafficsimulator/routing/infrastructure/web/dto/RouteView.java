package com.trafficsimulator.routing.infrastructure.web.dto;

import com.trafficsimulator.routing.domain.model.Route;

import java.util.List;

public record RouteView(boolean found, List<String> streets, List<String> nodes, double totalCost) {
    public static RouteView from(Route r) {
        return new RouteView(r.found(), r.streetIds(), r.nodePath(), r.totalCost());
    }
}
