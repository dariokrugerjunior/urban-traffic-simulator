package com.trafficsimulator.trafficstate.infrastructure.config;

import com.trafficsimulator.trafficstate.application.AddTrafficLightUseCase;
import com.trafficsimulator.trafficstate.application.GetTrafficSnapshotUseCase;
import com.trafficsimulator.trafficstate.application.InjectFlowUseCase;
import com.trafficsimulator.trafficstate.application.ReleaseFlowUseCase;
import com.trafficsimulator.trafficstate.domain.port.StreetCongestionPublisher;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import com.trafficsimulator.trafficstate.domain.port.TrafficStateBroadcaster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the framework-free application use cases from their domain ports. */
@Configuration
public class UseCaseConfig {

    @Bean
    InjectFlowUseCase injectFlowUseCase(StreetRepository repo, StreetCongestionPublisher pub, TrafficStateBroadcaster bc) {
        return new InjectFlowUseCase(repo, pub, bc);
    }

    @Bean
    ReleaseFlowUseCase releaseFlowUseCase(StreetRepository repo, StreetCongestionPublisher pub, TrafficStateBroadcaster bc) {
        return new ReleaseFlowUseCase(repo, pub, bc);
    }

    @Bean
    AddTrafficLightUseCase addTrafficLightUseCase(StreetRepository repo, StreetCongestionPublisher pub, TrafficStateBroadcaster bc) {
        return new AddTrafficLightUseCase(repo, pub, bc);
    }

    @Bean
    GetTrafficSnapshotUseCase getTrafficSnapshotUseCase(StreetRepository repo) {
        return new GetTrafficSnapshotUseCase(repo);
    }
}
