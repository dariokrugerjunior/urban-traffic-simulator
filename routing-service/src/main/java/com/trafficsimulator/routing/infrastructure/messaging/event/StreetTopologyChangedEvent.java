package com.trafficsimulator.routing.infrastructure.messaging.event;

import java.time.Instant;

/**
 * Consumed from the traffic-state-service. Field names must match the producer's record.
 * Flags are nullable (a partial edit carries only the changed field); routing only uses
 * {@code blocked} for now.
 */
public record StreetTopologyChangedEvent(String streetId, Boolean oneway, Boolean blocked,
                                         Boolean source, Instant occurredAt) { }
