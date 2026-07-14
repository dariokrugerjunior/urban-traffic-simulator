package com.trafficsimulator.trafficstate.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables the @Scheduled simulation tick/flush. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
