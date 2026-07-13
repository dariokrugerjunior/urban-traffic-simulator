package com.trafficsimulator.trafficstate.infrastructure.messaging;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetCongestionPublisher;
import com.trafficsimulator.trafficstate.infrastructure.config.KafkaTopicsConfig;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.StreetCongestedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class KafkaStreetCongestionPublisher implements StreetCongestionPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaStreetCongestionPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishCongested(Street street) {
        StreetCongestedEvent event = new StreetCongestedEvent(
                street.id(), street.name(), street.congestionLevel().name(),
                street.congestionRatio(), Instant.now());
        kafkaTemplate.send(KafkaTopicsConfig.STREET_CONGESTED, street.id(), event);
    }
}
