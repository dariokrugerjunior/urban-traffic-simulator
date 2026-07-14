package com.trafficsimulator.trafficstate.infrastructure.messaging;

import com.trafficsimulator.trafficstate.infrastructure.config.KafkaTopicsConfig;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.StreetTopologyChangedEvent;
import com.trafficsimulator.trafficstate.infrastructure.simulation.SimulationEngine;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Applies a structural edit to the live simulation graph, reacting to the topology event
 * (rather than mutating synchronously in the controller — same event-driven pattern as flow).
 */
@Component
public class StreetTopologyChangedListener {

    private final SimulationEngine engine;

    public StreetTopologyChangedListener(SimulationEngine engine) {
        this.engine = engine;
    }

    @KafkaListener(topics = KafkaTopicsConfig.STREET_TOPOLOGY_CHANGED, groupId = "traffic-state-service")
    public void on(StreetTopologyChangedEvent event) {
        engine.applyTopology(event.streetId(), event.oneway(), event.blocked(), event.source());
    }
}
