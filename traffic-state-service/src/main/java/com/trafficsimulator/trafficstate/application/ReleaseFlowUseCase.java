package com.trafficsimulator.trafficstate.application;

import com.trafficsimulator.trafficstate.domain.model.CongestionLevel;
import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetCongestionPublisher;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import com.trafficsimulator.trafficstate.domain.port.TrafficStateBroadcaster;

/** Removes vehicles from a street and reacts when it clears up (leaves JAMMED). */
public class ReleaseFlowUseCase {

    private final StreetRepository repository;
    private final StreetCongestionPublisher congestionPublisher;
    private final TrafficStateBroadcaster broadcaster;

    public ReleaseFlowUseCase(StreetRepository repository,
                              StreetCongestionPublisher congestionPublisher,
                              TrafficStateBroadcaster broadcaster) {
        this.repository = repository;
        this.congestionPublisher = congestionPublisher;
        this.broadcaster = broadcaster;
    }

    public void release(String streetId, int vehicles) {
        Street street = repository.findById(streetId)
                .orElseThrow(() -> new StreetNotFoundException(streetId));

        CongestionLevel before = street.congestionLevel();
        street.releaseFlow(vehicles);
        Street saved = repository.save(street);

        if (before == CongestionLevel.JAMMED && saved.congestionLevel() != CongestionLevel.JAMMED) {
            congestionPublisher.publishCleared(saved);
        }
        broadcaster.broadcast(saved);
    }
}
