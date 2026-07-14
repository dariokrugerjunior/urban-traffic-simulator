package com.trafficsimulator.trafficstate.application;

import com.trafficsimulator.trafficstate.application.support.InMemoryStreetRepository;
import com.trafficsimulator.trafficstate.application.support.RecordingBroadcaster;
import com.trafficsimulator.trafficstate.application.support.RecordingCongestionPublisher;
import com.trafficsimulator.trafficstate.domain.model.CongestionLevel;
import com.trafficsimulator.trafficstate.domain.model.Street;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseFlowUseCaseTest {

    @Test
    void publishesClearedWhenStreetLeavesJammed() {
        Street street = new Street("s1", "Main", "A", "B", 100, 90); // JAMMED
        var repo = new InMemoryStreetRepository(street);
        var pub = new RecordingCongestionPublisher();
        var bc = new RecordingBroadcaster();

        new ReleaseFlowUseCase(repo, pub, bc).release("s1", 60); // 30 left -> FREE

        assertEquals(CongestionLevel.FREE, repo.findById("s1").orElseThrow().congestionLevel());
        assertEquals(1, pub.cleared.size());
        assertEquals(1, bc.broadcasts.size());
    }

    @Test
    void doesNotClearWhenStillJammed() {
        Street street = new Street("s1", "Main", "A", "B", 100, 95); // JAMMED
        var repo = new InMemoryStreetRepository(street);
        var pub = new RecordingCongestionPublisher();
        var bc = new RecordingBroadcaster();

        new ReleaseFlowUseCase(repo, pub, bc).release("s1", 5); // 90 left -> still JAMMED

        assertTrue(pub.cleared.isEmpty());
    }

    @Test
    void volumeNeverGoesNegative() {
        Street street = new Street("s1", "Main", "A", "B", 100, 10);
        var repo = new InMemoryStreetRepository(street);

        new ReleaseFlowUseCase(repo, new RecordingCongestionPublisher(), new RecordingBroadcaster())
                .release("s1", 50);

        assertEquals(0, repo.findById("s1").orElseThrow().currentVolume());
    }
}
