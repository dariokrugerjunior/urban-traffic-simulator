package com.trafficsimulator.trafficstate.infrastructure.messaging;

import com.trafficsimulator.trafficstate.application.ReleaseFlowUseCase;
import com.trafficsimulator.trafficstate.infrastructure.config.KafkaTopicsConfig;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.FlowReleasedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FlowReleasedListener {
    private final ReleaseFlowUseCase useCase;

    public FlowReleasedListener(ReleaseFlowUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = KafkaTopicsConfig.FLOW_RELEASED, groupId = "traffic-state-service")
    public void on(FlowReleasedEvent event) {
        useCase.release(event.streetId(), event.vehicles());
    }
}
