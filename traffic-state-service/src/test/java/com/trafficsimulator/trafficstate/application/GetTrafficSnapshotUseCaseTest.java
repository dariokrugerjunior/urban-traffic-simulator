package com.trafficsimulator.trafficstate.application;

import com.trafficsimulator.trafficstate.application.support.InMemoryStreetRepository;
import com.trafficsimulator.trafficstate.domain.model.Street;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetTrafficSnapshotUseCaseTest {
    @Test
    void returnsAllStreets() {
        var repo = new InMemoryStreetRepository(
            new Street("s1", "A", "I1", "I2", 100),
            new Street("s2", "B", "I2", "I3", 100));
        assertEquals(2, new GetTrafficSnapshotUseCase(repo).currentState().size());
    }
}
