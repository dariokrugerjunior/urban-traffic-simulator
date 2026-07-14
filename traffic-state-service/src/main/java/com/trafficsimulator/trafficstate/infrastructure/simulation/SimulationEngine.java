package com.trafficsimulator.trafficstate.infrastructure.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficsimulator.trafficstate.domain.model.SimulationNetwork;
import com.trafficsimulator.trafficstate.domain.model.Street;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the in-memory road graph and runs the macroscopic simulation. This is the runtime
 * source of truth for street state; the JPA adapter remains a (tested) persistence capability
 * but is off the per-tick hot path.
 *
 * <p>All mutation (the tick and repository operations) is serialized on this engine's monitor.
 */
@Component
public class SimulationEngine {

    private final SimulationNetwork network = new SimulationNetwork();

    private final double outRate;
    private final double decay;
    private final int sourceRate;

    private final int sourceCount;

    public SimulationEngine(
            @Value("${simulation.out-rate:0.18}") double outRate,
            @Value("${simulation.decay:0.02}") double decay,
            @Value("${simulation.source-rate:900}") int sourceRate,
            @Value("${simulation.source-count:14}") int sourceCount) {
        this.outRate = outRate;
        this.decay = decay;
        this.sourceRate = sourceRate;
        this.sourceCount = sourceCount;
    }

    @PostConstruct
    void load() {
        // Curated I1→I5 corridor (keeps its abstract nodes so routing still works).
        add(new Street("st-beira-rio", "Av. Hermann August Lepper (Beira-Rio)", "I1", "I5", 2000));
        add(new Street("st-joao-colin", "Rua Joao Colin", "I1", "I3", 1800));
        add(new Street("st-dona-francisca", "Rua Dona Francisca", "I3", "I5", 1400));
        add(new Street("st-nove-de-marco", "Rua Nove de Marco", "I1", "I2", 900));
        add(new Street("st-xv-de-novembro", "Rua XV de Novembro", "I2", "I3", 1000));

        // Real central-Joinville graph.
        for (Edge e : readEdges("/city-network.json")) {
            String name = (e.name() == null || e.name().isBlank()) ? "Rua sem nome" : e.name();
            Street s = new Street(e.id(), name, e.nodeA(), e.nodeB(), e.capacity());
            s.setOneway(e.oneway());
            add(s);
        }

        seedDefaultSources();
    }

    private void add(Street s) {
        network.addStreet(s);
    }

    /** Mark a few high-capacity inbound streets as sources so the city is alive on boot. */
    private void seedDefaultSources() {
        List<Street> byCapacity = new ArrayList<>(network.streets());
        byCapacity.sort((a, b) -> Integer.compare(b.hourlyCapacity(), a.hourlyCapacity()));
        int sources = Math.min(sourceCount, byCapacity.size());
        for (int i = 0; i < sources; i++) {
            byCapacity.get(i).setSource(true);
        }
    }

    private Edge[] readEdges(String resource) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                return new Edge[0];
            }
            return new ObjectMapper().readValue(in, Edge[].class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + resource, e);
        }
    }

    /** Advances the simulation by one tick. Thread-safe. */
    public synchronized void tick() {
        network.tick(outRate, decay, sourceRate);
    }

    /** Runs {@code action} with exclusive access to the network (for reads/mutations). */
    public synchronized <T> T withNetwork(java.util.function.Function<SimulationNetwork, T> action) {
        return action.apply(network);
    }

    private record Edge(String id, String name, int capacity, String nodeA, String nodeB, boolean oneway) { }
}
