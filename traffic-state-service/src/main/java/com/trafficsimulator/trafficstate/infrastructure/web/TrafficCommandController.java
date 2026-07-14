package com.trafficsimulator.trafficstate.infrastructure.web;

import com.trafficsimulator.trafficstate.application.GetTrafficSnapshotUseCase;
import com.trafficsimulator.trafficstate.infrastructure.config.KafkaTopicsConfig;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.FlowInjectedEvent;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.FlowReleasedEvent;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.StreetTopologyChangedEvent;
import com.trafficsimulator.trafficstate.infrastructure.messaging.event.TrafficLightAddedEvent;
import com.trafficsimulator.trafficstate.infrastructure.web.dto.AddTrafficLightRequest;
import com.trafficsimulator.trafficstate.infrastructure.web.dto.InjectFlowRequest;
import com.trafficsimulator.trafficstate.infrastructure.web.dto.ReleaseFlowRequest;
import com.trafficsimulator.trafficstate.infrastructure.web.dto.StreetStateView;
import com.trafficsimulator.trafficstate.infrastructure.web.dto.UpdateTopologyRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST commands that translate into asynchronous Kafka events. The service reacts to
 * its own events (via listeners) rather than mutating state synchronously here.
 */
@RestController
@RequestMapping("/api/traffic")
public class TrafficCommandController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final GetTrafficSnapshotUseCase snapshotUseCase;

    public TrafficCommandController(KafkaTemplate<String, Object> kafkaTemplate,
                                    GetTrafficSnapshotUseCase snapshotUseCase) {
        this.kafkaTemplate = kafkaTemplate;
        this.snapshotUseCase = snapshotUseCase;
    }

    @PostMapping("/streets/{id}/flow")
    public ResponseEntity<Void> injectFlow(@PathVariable String id, @RequestBody InjectFlowRequest request) {
        kafkaTemplate.send(KafkaTopicsConfig.FLOW_INJECTED, id,
                new FlowInjectedEvent(id, request.vehicles(), Instant.now()));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/streets/{id}/release")
    public ResponseEntity<Void> releaseFlow(@PathVariable String id, @RequestBody ReleaseFlowRequest request) {
        kafkaTemplate.send(KafkaTopicsConfig.FLOW_RELEASED, id,
                new FlowReleasedEvent(id, request.vehicles(), Instant.now()));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/streets/{id}/traffic-light")
    public ResponseEntity<Void> addTrafficLight(@PathVariable String id,
                                                @RequestBody(required = false) AddTrafficLightRequest request) {
        double greenRatio = request == null ? 0.5 : request.greenRatioOrDefault();
        kafkaTemplate.send(KafkaTopicsConfig.TRAFFIC_LIGHT_ADDED, id,
                new TrafficLightAddedEvent(id, greenRatio, Instant.now()));
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/streets/{id}/topology")
    public ResponseEntity<Void> updateTopology(@PathVariable String id,
                                               @RequestBody UpdateTopologyRequest request) {
        kafkaTemplate.send(KafkaTopicsConfig.STREET_TOPOLOGY_CHANGED, id,
                new StreetTopologyChangedEvent(id, request.oneway(), request.blocked(),
                        request.source(), Instant.now()));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/streets")
    public List<StreetStateView> snapshot() {
        return snapshotUseCase.currentState().stream().map(StreetStateView::from).toList();
    }
}
