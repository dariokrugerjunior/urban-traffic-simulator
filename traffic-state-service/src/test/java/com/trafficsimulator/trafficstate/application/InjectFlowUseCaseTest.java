package com.trafficsimulator.trafficstate.application;

import com.trafficsimulator.trafficstate.application.support.InMemoryStreetRepository;
import com.trafficsimulator.trafficstate.application.support.RecordingBroadcaster;
import com.trafficsimulator.trafficstate.application.support.RecordingCongestionPublisher;
import com.trafficsimulator.trafficstate.domain.model.CongestionLevel;
import com.trafficsimulator.trafficstate.domain.model.Street;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InjectFlowUseCaseTest {

    private InjectFlowUseCase useCaseFor(InMemoryStreetRepository repo,
                                         RecordingCongestionPublisher pub,
                                         RecordingBroadcaster bc) {
        return new InjectFlowUseCase(repo, pub, bc);
    }

    @Test
    void publishesCongestedWhenStreetTransitionsIntoJammed() {
        Street street = new Street("s1", "Main", "A", "B", 100, 0);
        var repo = new InMemoryStreetRepository(street);
        var pub = new RecordingCongestionPublisher();
        var bc = new RecordingBroadcaster();

        useCaseFor(repo, pub, bc).inject("s1", 90); // 0.9 -> JAMMED

        assertEquals(CongestionLevel.JAMMED, repo.findById("s1").orElseThrow().congestionLevel());
        assertEquals(1, pub.published.size());
        assertEquals(1, bc.broadcasts.size());
    }

    @Test
    void doesNotPublishWhenStayingBelowJammed() {
        Street street = new Street("s1", "Main", "A", "B", 100, 0);
        var repo = new InMemoryStreetRepository(street);
        var pub = new RecordingCongestionPublisher();
        var bc = new RecordingBroadcaster();

        useCaseFor(repo, pub, bc).inject("s1", 40); // FREE

        assertTrue(pub.published.isEmpty());
        assertEquals(1, bc.broadcasts.size()); // always broadcast
    }

    @Test
    void doesNotRepublishWhenAlreadyJammed() {
        Street street = new Street("s1", "Main", "A", "B", 100, 90); // already JAMMED
        var repo = new InMemoryStreetRepository(street);
        var pub = new RecordingCongestionPublisher();
        var bc = new RecordingBroadcaster();

        useCaseFor(repo, pub, bc).inject("s1", 10); // still JAMMED

        assertTrue(pub.published.isEmpty()); // no NEW transition
    }

    @Test
    void throwsWhenStreetUnknown() {
        var repo = new InMemoryStreetRepository();
        assertThrows(StreetNotFoundException.class,
            () -> useCaseFor(repo, new RecordingCongestionPublisher(), new RecordingBroadcaster())
                    .inject("missing", 10));
    }
}
