package com.trafficsimulator.trafficstate.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {
    public static final String FLOW_INJECTED = "flow-injected";
    public static final String FLOW_RELEASED = "flow-released";
    public static final String TRAFFIC_LIGHT_ADDED = "traffic-light-added";
    public static final String STREET_CONGESTED = "street-congested";
    public static final String STREET_CLEARED = "street-cleared";
    public static final String STREET_TOPOLOGY_CHANGED = "street-topology-changed";

    @Bean
    NewTopic flowInjectedTopic() {
        return TopicBuilder.name(FLOW_INJECTED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic flowReleasedTopic() {
        return TopicBuilder.name(FLOW_RELEASED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic trafficLightAddedTopic() {
        return TopicBuilder.name(TRAFFIC_LIGHT_ADDED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic streetCongestedTopic() {
        return TopicBuilder.name(STREET_CONGESTED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic streetClearedTopic() {
        return TopicBuilder.name(STREET_CLEARED).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic streetTopologyChangedTopic() {
        return TopicBuilder.name(STREET_TOPOLOGY_CHANGED).partitions(1).replicas(1).build();
    }
}
