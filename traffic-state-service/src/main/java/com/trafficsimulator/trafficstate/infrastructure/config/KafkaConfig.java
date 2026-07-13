package com.trafficsimulator.trafficstate.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

/**
 * Consumes JSON values as plain strings and lets a {@link StringJsonMessageConverter}
 * infer the target type from each {@code @KafkaListener} method signature. This keeps
 * event DTOs duplicated per service (no shared type headers) while still routing each
 * topic to the correct record type.
 */
@Configuration
public class KafkaConfig {

    @Bean
    RecordMessageConverter jsonMessageConverter() {
        return new StringJsonMessageConverter();
    }
}
