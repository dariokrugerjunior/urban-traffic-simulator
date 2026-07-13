package com.trafficsimulator.trafficstate.application;

import com.trafficsimulator.trafficstate.domain.model.CongestionLevel;
import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetCongestionPublisher;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import com.trafficsimulator.trafficstate.domain.port.TrafficStateBroadcaster;

/** Applies an injected traffic flow to a street and reacts to congestion changes. */
public class InjectFlowUseCase {

    private final StreetRepository repository;
    private final StreetCongestionPublisher congestionPublisher;
    private final TrafficStateBroadcaster broadcaster;

    public InjectFlowUseCase(StreetRepository repository,
                             StreetCongestionPublisher congestionPublisher,
                             TrafficStateBroadcaster broadcaster) {
        this.repository = repository;
        this.congestionPublisher = congestionPublisher;
        this.broadcaster = broadcaster;
    }

    public void inject(String streetId, int vehicles) {
        Street street = repository.findById(streetId)
                .orElseThrow(() -> new StreetNotFoundException(streetId));

        CongestionLevel before = street.congestionLevel();
        street.injectFlow(vehicles);
        Street saved = repository.save(street);
        CongestionLevel after = saved.congestionLevel();

        if (after == CongestionLevel.JAMMED && before != CongestionLevel.JAMMED) {
            congestionPublisher.publishCongested(saved);
        }
        broadcaster.broadcast(saved);
    }
}
