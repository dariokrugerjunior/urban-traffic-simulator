package com.trafficsimulator.trafficstate.infrastructure.messaging.event;

import java.time.Instant;

/**
 * A structural edit to a street. Each flag is nullable so a single-field toggle (e.g. only
 * flipping one-way) carries just that field; null fields are left unchanged.
 */
public record StreetTopologyChangedEvent(String streetId,
                                         Boolean oneway,
                                         Boolean blocked,
                                         Boolean source,
                                         Instant occurredAt) { }
