package com.trafficsimulator.routing.infrastructure.messaging;

import com.trafficsimulator.routing.application.PenalizeStreetUseCase;
import com.trafficsimulator.routing.infrastructure.messaging.event.StreetCongestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StreetCongestedListener {
    private final PenalizeStreetUseCase useCase;

    public StreetCongestedListener(PenalizeStreetUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "street-congested", groupId = "routing-service")
    public void on(StreetCongestedEvent event) {
        useCase.penalize(event.streetId());
    }
}
