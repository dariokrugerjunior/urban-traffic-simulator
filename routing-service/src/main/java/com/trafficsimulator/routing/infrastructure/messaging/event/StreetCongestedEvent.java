package com.trafficsimulator.routing.infrastructure.messaging.event;

import java.time.Instant;

/** Consumed from the traffic-state-service; field names must match the producer's record. */
public record StreetCongestedEvent(String streetId, String name, String congestionLevel,
                                   double ratio, Instant occurredAt) { }
