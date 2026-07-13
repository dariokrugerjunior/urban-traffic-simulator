package com.trafficsimulator.trafficstate.application;

import com.trafficsimulator.trafficstate.domain.model.CongestionLevel;
import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetCongestionPublisher;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import com.trafficsimulator.trafficstate.domain.port.TrafficStateBroadcaster;

/** Adds a traffic light to a street, reducing throughput, and reacts to congestion changes. */
public class AddTrafficLightUseCase {

    private final StreetRepository repository;
    private final StreetCongestionPublisher congestionPublisher;
    private final TrafficStateBroadcaster broadcaster;

    public AddTrafficLightUseCase(StreetRepository repository,
                                  StreetCongestionPublisher congestionPublisher,
                                  TrafficStateBroadcaster broadcaster) {
        this.repository = repository;
        this.congestionPublisher = congestionPublisher;
        this.broadcaster = broadcaster;
    }

    public void addTrafficLight(String streetId, double greenRatio) {
        Street street = repository.findById(streetId)
                .orElseThrow(() -> new StreetNotFoundException(streetId));

        CongestionLevel before = street.congestionLevel();
        street.addTrafficLight(greenRatio);
        Street saved = repository.save(street);

        if (saved.congestionLevel() == CongestionLevel.JAMMED && before != CongestionLevel.JAMMED) {
            congestionPublisher.publishCongested(saved);
        }
        broadcaster.broadcast(saved);
    }
}
