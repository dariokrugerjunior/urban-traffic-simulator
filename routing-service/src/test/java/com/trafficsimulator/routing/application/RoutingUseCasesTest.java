package com.trafficsimulator.routing.application;

import com.trafficsimulator.routing.domain.model.DijkstraPathFinder;
import com.trafficsimulator.routing.domain.model.Intersection;
import com.trafficsimulator.routing.domain.model.RoadNetwork;
import com.trafficsimulator.routing.domain.model.RouteStreet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutingUseCasesTest {

    private RoadNetwork joinville() {
        RoadNetwork n = new RoadNetwork();
        for (String id : new String[]{"I1", "I3", "I5"}) {
            n.addIntersection(new Intersection(id, id));
        }
        n.addStreet(new RouteStreet("st-beira-rio", "Beira-Rio", "I1", "I5", 5));
        n.addStreet(new RouteStreet("st-joao-colin", "Joao Colin", "I1", "I3", 3));
        n.addStreet(new RouteStreet("st-dona-francisca", "Dona Francisca", "I3", "I5", 3));
        return n;
    }

    @Test
    void penalizingReroutesSubsequentFindRoute() {
        RoadNetwork n = joinville();
        var find = new FindRouteUseCase(n, new DijkstraPathFinder());
        assertEquals("st-beira-rio", find.find("I1", "I5").streetIds().get(0));

        new PenalizeStreetUseCase(n).penalize("st-beira-rio");

        assertEquals(List.of("st-joao-colin", "st-dona-francisca"),
                find.find("I1", "I5").streetIds());
    }

    @Test
    void getNetworkStateExposesWeights() {
        RoadNetwork n = joinville();
        new PenalizeStreetUseCase(n).penalize("st-beira-rio");
        List<RouteStreet> state = new GetNetworkStateUseCase(n).streets();
        assertEquals(3, state.size());
        assertEquals(50.0,
            state.stream().filter(s -> s.id().equals("st-beira-rio")).findFirst().orElseThrow().weight(), 1e-9);
    }
}
