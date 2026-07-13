package com.trafficsimulator.trafficstate.application;

import com.trafficsimulator.trafficstate.application.support.InMemoryStreetRepository;
import com.trafficsimulator.trafficstate.application.support.RecordingBroadcaster;
import com.trafficsimulator.trafficstate.application.support.RecordingCongestionPublisher;
import com.trafficsimulator.trafficstate.domain.model.Street;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddTrafficLightUseCaseTest {

    @Test
    void addingLightMayPushStreetIntoJammedAndPublish() {
        Street street = new Street("s1", "Main", "A", "B", 100, 45); // FREE (0.45)
        var repo = new InMemoryStreetRepository(street);
        var pub = new RecordingCongestionPublisher();
        var bc = new RecordingBroadcaster();

        new AddTrafficLightUseCase(repo, pub, bc).addTrafficLight("s1", 0.5);
        // effective 50, ratio 0.9 -> JAMMED

        assertEquals(1, repo.findById("s1").orElseThrow().trafficLightCount());
        assertEquals(1, pub.published.size());
        assertEquals(1, bc.broadcasts.size());
    }

    @Test
    void throwsWhenStreetUnknown() {
        var repo = new InMemoryStreetRepository();
        assertThrows(StreetNotFoundException.class,
            () -> new AddTrafficLightUseCase(repo, new RecordingCongestionPublisher(), new RecordingBroadcaster())
                    .addTrafficLight("missing", 0.5));
    }
}
