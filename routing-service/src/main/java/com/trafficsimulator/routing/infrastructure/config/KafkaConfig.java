package com.trafficsimulator.routing.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

/**
 * Consumes JSON values as plain strings and lets a {@link StringJsonMessageConverter}
 * infer the target type from the {@code @KafkaListener} method signature — so the
 * event DTO can be duplicated here without sharing type headers with the producer.
 */
@Configuration
public class KafkaConfig {

    @Bean
    RecordMessageConverter jsonMessageConverter() {
        return new StringJsonMessageConverter();
    }
}
