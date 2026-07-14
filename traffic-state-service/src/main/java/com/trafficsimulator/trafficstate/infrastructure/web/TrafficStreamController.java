package com.trafficsimulator.trafficstate.infrastructure.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE endpoint streaming real-time street-state updates to the frontend.
 *
 * <p>The initial snapshot is intentionally NOT replayed here — clients load it once via
 * {@code GET /api/traffic/streets}. With thousands of edges, replaying it as individual SSE
 * events on every connect would flood the UI with re-renders.
 */
@RestController
@RequestMapping("/api/traffic")
public class TrafficStreamController {

    private final SseTrafficBroadcaster broadcaster;

    public TrafficStreamController(SseTrafficBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return broadcaster.register();
    }
}
