package com.trafficsimulator.routing.infrastructure.messaging;

import com.trafficsimulator.routing.application.SetStreetBlockedUseCase;
import com.trafficsimulator.routing.infrastructure.messaging.event.StreetTopologyChangedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Closes/reopens a street in the routing graph when traffic-state reports a topology edit. */
@Component
public class StreetTopologyChangedListener {
    private final SetStreetBlockedUseCase useCase;

    public StreetTopologyChangedListener(SetStreetBlockedUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "street-topology-changed", groupId = "routing-service")
    public void on(StreetTopologyChangedEvent event) {
        if (event.blocked() != null) {
            useCase.setBlocked(event.streetId(), event.blocked());
        }
    }
}
