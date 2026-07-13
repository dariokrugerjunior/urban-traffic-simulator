package com.trafficsimulator.routing.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DijkstraPathFinderTest {

    private RoadNetwork joinville() {
        RoadNetwork n = new RoadNetwork();
        for (String id : new String[]{"I1", "I2", "I3", "I5"}) {
            n.addIntersection(new Intersection(id, id));
        }
        n.addStreet(new RouteStreet("st-beira-rio", "Beira-Rio", "I1", "I5", 5));
        n.addStreet(new RouteStreet("st-joao-colin", "Joao Colin", "I1", "I3", 3));
        n.addStreet(new RouteStreet("st-dona-francisca", "Dona Francisca", "I3", "I5", 3));
        n.addStreet(new RouteStreet("st-nove-de-marco", "Nove de Marco", "I1", "I2", 2));
        n.addStreet(new RouteStreet("st-xv-de-novembro", "XV de Novembro", "I2", "I3", 2));
        return n;
    }

    private final DijkstraPathFinder finder = new DijkstraPathFinder();

    @Test
    void picksDirectBeiraRioWhenUncongested() {
        Route route = finder.findShortest(joinville(), "I1", "I5");
        assertTrue(route.found());
        assertEquals(5.0, route.totalCost(), 1e-9);
        assertEquals(List.of("st-beira-rio"), route.streetIds());
    }

    @Test
    void reroutesAroundBeiraRioWhenPenalized() {
        RoadNetwork n = joinville();
        n.penalize("st-beira-rio", 10); // weight 50
        Route route = finder.findShortest(n, "I1", "I5");
        assertTrue(route.found());
        assertEquals(6.0, route.totalCost(), 1e-9); // joao-colin(3) + dona-francisca(3)
        assertEquals(List.of("st-joao-colin", "st-dona-francisca"), route.streetIds());
    }

    @Test
    void returnsNotFoundWhenNoPath() {
        Route route = finder.findShortest(joinville(), "I5", "I1"); // all edges directed away
        assertFalse(route.found());
    }
}
