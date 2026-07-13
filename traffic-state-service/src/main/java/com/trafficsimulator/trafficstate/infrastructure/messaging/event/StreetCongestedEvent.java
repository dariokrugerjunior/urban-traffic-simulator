package com.trafficsimulator.trafficstate.infrastructure.messaging.event;

import java.time.Instant;

public record StreetCongestedEvent(String streetId, String name, String congestionLevel,
                                   double ratio, Instant occurredAt) { }
