package com.trafficsimulator.routing.domain.model;

import java.util.List;

/** Result of a shortest-path query: the ordered streets/nodes and the total cost. */
public record Route(List<String> streetIds, List<String> nodePath, double totalCost, boolean found) {
    public static Route notFound() {
        return new Route(List.of(), List.of(), Double.POSITIVE_INFINITY, false);
    }
}
