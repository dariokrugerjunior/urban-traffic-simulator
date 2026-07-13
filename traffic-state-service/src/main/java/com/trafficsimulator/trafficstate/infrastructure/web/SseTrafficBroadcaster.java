package com.trafficsimulator.trafficstate.infrastructure.web;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.TrafficStateBroadcaster;
import com.trafficsimulator.trafficstate.infrastructure.web.dto.StreetStateView;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Fan-out adapter pushing street-state updates to all connected SSE subscribers. */
@Component
public class SseTrafficBroadcaster implements TrafficStateBroadcaster {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    @Override
    public void broadcast(Street street) {
        StreetStateView view = StreetStateView.from(street);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("street-update").data(view));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
