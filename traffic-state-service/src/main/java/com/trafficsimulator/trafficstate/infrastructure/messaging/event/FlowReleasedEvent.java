package com.trafficsimulator.trafficstate.infrastructure.messaging.event;

import java.time.Instant;

public record FlowReleasedEvent(String streetId, int vehicles, Instant occurredAt) { }
