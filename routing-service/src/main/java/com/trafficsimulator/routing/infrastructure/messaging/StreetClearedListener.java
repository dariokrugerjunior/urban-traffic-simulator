package com.trafficsimulator.routing.infrastructure.messaging;

import com.trafficsimulator.routing.application.ClearStreetUseCase;
import com.trafficsimulator.routing.infrastructure.messaging.event.StreetClearedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StreetClearedListener {
    private final ClearStreetUseCase useCase;

    public StreetClearedListener(ClearStreetUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "street-cleared", groupId = "routing-service")
    public void on(StreetClearedEvent event) {
        useCase.clear(event.streetId());
    }
}
