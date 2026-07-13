package com.trafficsimulator.trafficstate.infrastructure.messaging;

import com.trafficsimulator.trafficstate.application.AddTrafficLightUseCase;
import com.trafficsimulator.trafficstate.infrastructure.config.KafkaTopicsConfig;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.TrafficLightAddedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TrafficLightAddedListener {
    private final AddTrafficLightUseCase useCase;

    public TrafficLightAddedListener(AddTrafficLightUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = KafkaTopicsConfig.TRAFFIC_LIGHT_ADDED, groupId = "traffic-state-service")
    public void on(TrafficLightAddedEvent event) {
        useCase.addTrafficLight(event.streetId(), event.greenRatio());
    }
}
