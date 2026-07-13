package com.trafficsimulator.trafficstate.infrastructure.messaging;

import com.trafficsimulator.trafficstate.application.InjectFlowUseCase;
import com.trafficsimulator.trafficstate.infrastructure.config.KafkaTopicsConfig;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.FlowInjectedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FlowInjectedListener {
    private final InjectFlowUseCase useCase;

    public FlowInjectedListener(InjectFlowUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = KafkaTopicsConfig.FLOW_INJECTED, groupId = "traffic-state-service")
    public void on(FlowInjectedEvent event) {
        useCase.inject(event.streetId(), event.vehicles());
    }
}
