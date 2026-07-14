package com.trafficsimulator.trafficstate.infrastructure.messaging;

import com.trafficsimulator.trafficstate.domain.model.CongestionLevel;
import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import com.trafficsimulator.trafficstate.infrastructure.config.KafkaTopicsConfig;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.FlowInjectedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Instant;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "simulation.enabled=false"
})
@EmbeddedKafka(partitions = 1, topics = { KafkaTopicsConfig.FLOW_INJECTED })
class KafkaFlowRoundTripTest {

    @Autowired KafkaTemplate<String, Object> template;
    @Autowired StreetRepository repository;

    @Test
    void injectedFlowEventUpdatesStreetToJammed() {
        repository.save(new Street("st-beira-rio", "Beira-Rio", "I1", "I5", 100, 0));

        template.send(KafkaTopicsConfig.FLOW_INJECTED, "st-beira-rio",
            new FlowInjectedEvent("st-beira-rio", 95, Instant.now()));

        await().atMost(15, SECONDS).untilAsserted(() ->
            assertEquals(CongestionLevel.JAMMED,
                repository.findById("st-beira-rio").orElseThrow().congestionLevel()));
    }
}
