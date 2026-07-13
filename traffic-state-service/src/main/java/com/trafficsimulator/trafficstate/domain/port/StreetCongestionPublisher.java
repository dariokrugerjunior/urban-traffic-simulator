package com.trafficsimulator.trafficstate.domain.port;

import com.trafficsimulator.trafficstate.domain.model.Street;

/** Outbound port announcing that a street became congested (JAMMED). */
public interface StreetCongestionPublisher {
    void publishCongested(Street street);
}
