package com.trafficsimulator.trafficstate.infrastructure.messaging.event;

import java.time.Instant;

public record TrafficLightAddedEvent(String streetId, double greenRatio, Instant occurredAt) { }
