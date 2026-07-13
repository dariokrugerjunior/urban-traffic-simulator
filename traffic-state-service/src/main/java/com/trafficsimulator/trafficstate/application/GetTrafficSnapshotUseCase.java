package com.trafficsimulator.trafficstate.application;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import java.util.List;

/** Returns the current state of all known streets (SSE initial snapshot / inspection). */
public class GetTrafficSnapshotUseCase {
    private final StreetRepository repository;

    public GetTrafficSnapshotUseCase(StreetRepository repository) {
        this.repository = repository;
    }

    public List<Street> currentState() {
        return repository.findAll();
    }
}
