package com.trafficsimulator.routing.infrastructure.config;

import com.trafficsimulator.routing.application.ClearStreetUseCase;
import com.trafficsimulator.routing.application.FindRouteUseCase;
import com.trafficsimulator.routing.application.GetNetworkStateUseCase;
import com.trafficsimulator.routing.application.PenalizeStreetUseCase;
import com.trafficsimulator.routing.domain.model.DijkstraPathFinder;
import com.trafficsimulator.routing.domain.model.Intersection;
import com.trafficsimulator.routing.domain.model.RoadNetwork;
import com.trafficsimulator.routing.domain.model.RouteStreet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds the in-memory Joinville road network and wires the framework-free use cases.
 * Node ids and street ids MUST match the traffic-state-service seed so congestion
 * events line up with the graph.
 */
@Configuration
public class RoutingBeansConfig {

    @Bean
    public RoadNetwork roadNetwork() {
        RoadNetwork n = new RoadNetwork();
        n.addIntersection(new Intersection("I1", "Centro"));
        n.addIntersection(new Intersection("I2", "Estacao"));
        n.addIntersection(new Intersection("I3", "America"));
        n.addIntersection(new Intersection("I5", "Saguacu"));
        n.addStreet(new RouteStreet("st-beira-rio", "Av. Hermann August Lepper (Beira-Rio)", "I1", "I5", 5));
        n.addStreet(new RouteStreet("st-joao-colin", "Rua Joao Colin", "I1", "I3", 3));
        n.addStreet(new RouteStreet("st-dona-francisca", "Rua Dona Francisca", "I3", "I5", 3));
        n.addStreet(new RouteStreet("st-nove-de-marco", "Rua Nove de Marco", "I1", "I2", 2));
        n.addStreet(new RouteStreet("st-xv-de-novembro", "Rua XV de Novembro", "I2", "I3", 2));
        return n;
    }

    @Bean
    DijkstraPathFinder pathFinder() {
        return new DijkstraPathFinder();
    }

    @Bean
    FindRouteUseCase findRouteUseCase(RoadNetwork n, DijkstraPathFinder f) {
        return new FindRouteUseCase(n, f);
    }

    @Bean
    PenalizeStreetUseCase penalizeStreetUseCase(RoadNetwork n) {
        return new PenalizeStreetUseCase(n);
    }

    @Bean
    ClearStreetUseCase clearStreetUseCase(RoadNetwork n) {
        return new ClearStreetUseCase(n);
    }

    @Bean
    GetNetworkStateUseCase getNetworkStateUseCase(RoadNetwork n) {
        return new GetNetworkStateUseCase(n);
    }
}
