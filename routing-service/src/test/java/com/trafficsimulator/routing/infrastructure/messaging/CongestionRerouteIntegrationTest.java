package com.trafficsimulator.routing.infrastructure.messaging;

import com.trafficsimulator.routing.application.FindRouteUseCase;
import com.trafficsimulator.routing.infrastructure.messaging.event.StreetCongestedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Instant;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(partitions = 1, topics = { "street-congested" })
class CongestionRerouteIntegrationTest {

    @Autowired KafkaTemplate<String, Object> template;
    @Autowired FindRouteUseCase findRoute;

    @Test
    void congestionEventReroutesTheGpsEngine() {
        assertEquals("st-beira-rio", findRoute.find("I1", "I5").streetIds().get(0));

        template.send("street-congested", "st-beira-rio",
            new StreetCongestedEvent("st-beira-rio", "Beira-Rio", "JAMMED", 0.95, Instant.now()));

        await().atMost(15, SECONDS).untilAsserted(() ->
            assertEquals(List.of("st-joao-colin", "st-dona-francisca"),
                findRoute.find("I1", "I5").streetIds()));
    }
}
