package com.trafficsimulator.trafficstate.infrastructure.web;

import com.trafficsimulator.trafficstate.application.GetTrafficSnapshotUseCase;
import com.trafficsimulator.trafficstate.infrastructure.web.dto.StreetStateView;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/** SSE endpoint streaming real-time street-state updates to the frontend. */
@RestController
@RequestMapping("/api/traffic")
public class TrafficStreamController {

    private final SseTrafficBroadcaster broadcaster;
    private final GetTrafficSnapshotUseCase snapshotUseCase;

    public TrafficStreamController(SseTrafficBroadcaster broadcaster, GetTrafficSnapshotUseCase snapshotUseCase) {
        this.broadcaster = broadcaster;
        this.snapshotUseCase = snapshotUseCase;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = broadcaster.register();
        try {
            for (var street : snapshotUseCase.currentState()) {
                emitter.send(SseEmitter.event().name("street-update").data(StreetStateView.from(street)));
            }
        } catch (IOException ignored) {
            // client disconnected during the initial snapshot; the emitter is already cleaned up
        }
        return emitter;
    }
}
