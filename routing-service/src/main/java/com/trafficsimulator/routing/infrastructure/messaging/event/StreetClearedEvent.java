package com.trafficsimulator.routing.infrastructure.messaging.event;

import java.time.Instant;

/** Consumed from the traffic-state-service when a street leaves JAMMED. */
public record StreetClearedEvent(String streetId, String name, String congestionLevel,
                                 double ratio, Instant occurredAt) { }
