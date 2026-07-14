package com.trafficsimulator.trafficstate.infrastructure.messaging;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import com.trafficsimulator.trafficstate.infrastructure.config.KafkaTopicsConfig;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.StreetTopologyChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Instant;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "simulation.enabled=false"
})
@EmbeddedKafka(partitions = 1, topics = { KafkaTopicsConfig.STREET_TOPOLOGY_CHANGED })
class KafkaTopologyRoundTripTest {

    @Autowired KafkaTemplate<String, Object> template;
    @Autowired StreetRepository repository;

    @Test
    void topologyEventClosesTheStreet() {
        repository.save(new Street("st-topo", "Rua Teste", "A", "B", 100, 0));

        template.send(KafkaTopicsConfig.STREET_TOPOLOGY_CHANGED, "st-topo",
            new StreetTopologyChangedEvent("st-topo", null, true, null, Instant.now()));

        await().atMost(15, SECONDS).untilAsserted(() ->
            assertTrue(repository.findById("st-topo").orElseThrow().blocked(),
                "the street should be blocked after the topology event"));
    }
}
