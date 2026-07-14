package com.trafficsimulator.routing.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficsimulator.routing.application.ClearStreetUseCase;
import com.trafficsimulator.routing.application.FindRouteBetweenStreetsUseCase;
import com.trafficsimulator.routing.application.FindRouteUseCase;
import com.trafficsimulator.routing.application.GetNetworkStateUseCase;
import com.trafficsimulator.routing.application.PenalizeStreetUseCase;
import com.trafficsimulator.routing.application.SetStreetBlockedUseCase;
import com.trafficsimulator.routing.domain.model.DijkstraPathFinder;
import com.trafficsimulator.routing.domain.model.Intersection;
import com.trafficsimulator.routing.domain.model.RoadNetwork;
import com.trafficsimulator.routing.domain.model.RouteStreet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

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

        // Curated I1→I5 corridor (keeps the abstract nodes so the demo route still works).
        n.addIntersection(new Intersection("I1", "Centro"));
        n.addIntersection(new Intersection("I2", "Estacao"));
        n.addIntersection(new Intersection("I3", "America"));
        n.addIntersection(new Intersection("I5", "Saguacu"));
        n.addStreet(new RouteStreet("st-beira-rio", "Av. Hermann August Lepper (Beira-Rio)", "I1", "I5", 5));
        n.addStreet(new RouteStreet("st-joao-colin", "Rua Joao Colin", "I1", "I3", 3));
        n.addStreet(new RouteStreet("st-dona-francisca", "Rua Dona Francisca", "I3", "I5", 3));
        n.addStreet(new RouteStreet("st-nove-de-marco", "Rua Nove de Marco", "I1", "I2", 2));
        n.addStreet(new RouteStreet("st-xv-de-novembro", "Rua XV de Novembro", "I2", "I3", 2));

        loadCityGraph(n, "/routing-graph.json");
        return n;
    }

    /** Loads the real Joinville graph: one directed edge per one-way street, two per two-way. */
    private void loadCityGraph(RoadNetwork n, String resource) {
        for (Edge e : readEdges(resource)) {
            String name = (e.name() == null || e.name().isBlank()) ? "Rua sem nome" : e.name();
            double w = Math.max(1.0, e.weightMeters());
            n.addIntersection(new Intersection(e.nodeA(), e.nodeA()));
            n.addIntersection(new Intersection(e.nodeB(), e.nodeB()));
            n.addStreet(new RouteStreet(e.id(), name, e.nodeA(), e.nodeB(), w)); // canonical A→B
            if (!e.oneway()) {
                n.addStreet(new RouteStreet(e.id(), name, e.nodeB(), e.nodeA(), w)); // reverse B→A
            }
        }
    }

    private Edge[] readEdges(String resource) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                return new Edge[0];
            }
            return new ObjectMapper().readValue(in, Edge[].class);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load " + resource, ex);
        }
    }

    private record Edge(String id, String name, String nodeA, String nodeB,
                        boolean oneway, double weightMeters) { }

    @Bean
    DijkstraPathFinder pathFinder() {
        return new DijkstraPathFinder();
    }

    @Bean
    FindRouteUseCase findRouteUseCase(RoadNetwork n, DijkstraPathFinder f) {
        return new FindRouteUseCase(n, f);
    }

    @Bean
    FindRouteBetweenStreetsUseCase findRouteBetweenStreetsUseCase(RoadNetwork n, DijkstraPathFinder f) {
        return new FindRouteBetweenStreetsUseCase(n, f);
    }

    @Bean
    PenalizeStreetUseCase penalizeStreetUseCase(RoadNetwork n) {
        return new PenalizeStreetUseCase(n);
    }

    @Bean
    SetStreetBlockedUseCase setStreetBlockedUseCase(RoadNetwork n) {
        return new SetStreetBlockedUseCase(n);
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
