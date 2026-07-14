package com.trafficsimulator.trafficstate.infrastructure.simulation;

import com.trafficsimulator.trafficstate.domain.model.CongestionLevel;
import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetCongestionPublisher;
import com.trafficsimulator.trafficstate.infrastructure.web.SseTrafficBroadcaster;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Drives the living simulation: a fast tick advances the fluid, and a slower flush consolidates
 * the changes into one batched SSE event plus Kafka congestion transitions (requirement R1 —
 * the frontend only ever sees a coalesced update every few seconds).
 *
 * <p>Disabled in tests via {@code simulation.enabled=false}.
 */
@Component
@ConditionalOnProperty(name = "simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationScheduler {

    private final SimulationEngine engine;
    private final StreetCongestionPublisher congestionPublisher;
    private final SseTrafficBroadcaster broadcaster;
    private final Map<String, CongestionLevel> lastLevels = new HashMap<>();

    public SimulationScheduler(SimulationEngine engine,
                               StreetCongestionPublisher congestionPublisher,
                               SseTrafficBroadcaster broadcaster) {
        this.engine = engine;
        this.congestionPublisher = congestionPublisher;
        this.broadcaster = broadcaster;
    }

    @Scheduled(fixedRateString = "${simulation.tick-ms:1000}")
    public void tick() {
        engine.tick();
    }

    @Scheduled(fixedRateString = "${simulation.flush-ms:3000}", initialDelayString = "${simulation.flush-ms:3000}")
    public void flush() {
        List<Street> changed = new ArrayList<>();
        List<Street> nowCongested = new ArrayList<>();
        List<Street> nowCleared = new ArrayList<>();

        // Short critical section: detect level changes and record them.
        engine.withNetwork(network -> {
            for (Street s : network.streets()) {
                CongestionLevel level = s.congestionLevel();
                CongestionLevel previous = lastLevels.get(s.id());
                if (previous != level) {
                    changed.add(s);
                    if (level == CongestionLevel.JAMMED && previous != CongestionLevel.JAMMED) {
                        nowCongested.add(s);
                    } else if (previous == CongestionLevel.JAMMED && level != CongestionLevel.JAMMED) {
                        nowCleared.add(s);
                    }
                    lastLevels.put(s.id(), level);
                }
            }
            return null;
        });

        // Emit outside the lock.
        nowCongested.forEach(congestionPublisher::publishCongested);
        nowCleared.forEach(congestionPublisher::publishCleared);
        broadcaster.broadcastBatch(changed);
    }
}
