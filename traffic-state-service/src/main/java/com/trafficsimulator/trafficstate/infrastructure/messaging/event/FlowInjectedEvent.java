package com.trafficsimulator.trafficstate.infrastructure.messaging.event;

import java.time.Instant;

public record FlowInjectedEvent(String streetId, int vehicles, Instant occurredAt) { }
