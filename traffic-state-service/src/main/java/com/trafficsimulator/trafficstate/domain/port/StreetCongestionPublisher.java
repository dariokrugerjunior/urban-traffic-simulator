package com.trafficsimulator.trafficstate.domain.port;

import com.trafficsimulator.trafficstate.domain.model.Street;

/** Outbound port announcing congestion transitions of a street. */
public interface StreetCongestionPublisher {
    /** The street just became congested (entered JAMMED). */
    void publishCongested(Street street);

    /** The street just cleared up (left JAMMED). */
    void publishCleared(Street street);
}
