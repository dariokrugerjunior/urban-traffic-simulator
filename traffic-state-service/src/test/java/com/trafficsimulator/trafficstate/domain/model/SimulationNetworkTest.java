package com.trafficsimulator.trafficstate.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationNetworkTest {

    private Street street(String id, String from, String to, int capacity, boolean oneway) {
        Street s = new Street(id, id, from, to, capacity, 0);
        s.setOneway(oneway);
        return s;
    }

    @Test
    void propagationMovesVolumeDownstream() {
        SimulationNetwork net = new SimulationNetwork();
        Street s1 = street("s1", "n1", "n2", 100, true);
        s1.updateVolume(100);
        Street s2 = street("s2", "n2", "n3", 100, true);
        net.addStreet(s1);
        net.addStreet(s2);

        net.tick(1.0, 0.0, 0);

        assertEquals(0, s1.currentVolume());
        assertEquals(100, s2.currentVolume());
    }

    @Test
    void sinkDissipatesFlow() {
        SimulationNetwork net = new SimulationNetwork();
        Street s = street("s", "n1", "n2", 100, true); // n2 has no onward street -> sink
        s.updateVolume(80);
        net.addStreet(s);

        net.tick(1.0, 0.0, 0);

        assertEquals(0, s.currentVolume());
    }

    @Test
    void decayReducesIsolatedVolume() {
        SimulationNetwork net = new SimulationNetwork();
        Street s = street("s", "n1", "n2", 100, true);
        s.updateVolume(100);
        net.addStreet(s);

        net.tick(0.0, 0.1, 0); // no outflow, 10% decay

        assertEquals(90, s.currentVolume());
    }

    @Test
    void volumeNeverGoesNegative() {
        SimulationNetwork net = new SimulationNetwork();
        Street s = street("s", "n1", "n2", 100, true);
        s.updateVolume(5);
        net.addStreet(s);

        for (int i = 0; i < 10; i++) {
            net.tick(1.0, 0.2, 0);
        }

        assertTrue(s.currentVolume() >= 0);
    }

    @Test
    void aSourceGeneratesTrafficThatSpreadsAndThenClearsWhenStopped() {
        SimulationNetwork net = new SimulationNetwork();
        Street s1 = street("s1", "n1", "n2", 100, true);
        s1.setSource(true);
        Street s2 = street("s2", "n2", "n3", 100, true); // n3 is a sink
        net.addStreet(s1);
        net.addStreet(s2);

        for (int i = 0; i < 25; i++) {
            net.tick(0.3, 0.02, 60);
        }
        assertTrue(s2.currentVolume() > 0, "traffic should have spread downstream");

        s1.setSource(false);
        for (int i = 0; i < 300; i++) {
            net.tick(0.3, 0.05, 0);
        }
        assertEquals(CongestionLevel.FREE, s1.congestionLevel());
        assertEquals(CongestionLevel.FREE, s2.congestionLevel());
    }
}
