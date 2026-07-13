package com.trafficsimulator.trafficstate.domain.port;

import com.trafficsimulator.trafficstate.domain.model.Street;

/** Outbound port pushing a street's current state to real-time subscribers (SSE). */
public interface TrafficStateBroadcaster {
    void broadcast(Street street);
}
