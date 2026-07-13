package com.trafficsimulator.routing.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoadNetworkTest {

    private RoadNetwork network() {
        RoadNetwork n = new RoadNetwork();
        n.addIntersection(new Intersection("I1", "Centro"));
        n.addIntersection(new Intersection("I5", "Saguacu"));
        n.addStreet(new RouteStreet("st-beira-rio", "Beira-Rio", "I1", "I5", 5));
        return n;
    }

    @Test
    void outgoingReturnsEdgesFromNode() {
        assertEquals(1, network().outgoing("I1").size());
        assertTrue(network().outgoing("I5").isEmpty());
    }

    @Test
    void penalizeMultipliesEffectiveWeight() {
        RoadNetwork n = network();
        n.penalize("st-beira-rio", 10);
        assertEquals(50.0, n.outgoing("I1").get(0).weight(), 1e-9);
    }
}
