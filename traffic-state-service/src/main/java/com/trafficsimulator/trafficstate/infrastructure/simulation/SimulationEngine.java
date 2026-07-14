package com.trafficsimulator.trafficstate.infrastructure.simulation;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /** Ids edited by a topology change since the last flush, so the SSE batch force-includes them. */
    private final Set<String> topologyChanged = new LinkedHashSet<>();

    private final double outRate;
    private final double decay;
    private final int sourceRate;

    private final int sourceCount;
    private final double signalGreenRatio;

    /** Through-road classes that feed traffic into the city (candidate sources). */
    private static final Set<String> SOURCE_CLASSES = Set.of(
            "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link");

    public SimulationEngine(
            @Value("${simulation.out-rate:0.18}") double outRate,
            @Value("${simulation.decay:0.02}") double decay,
            @Value("${simulation.source-rate:900}") int sourceRate,
            @Value("${simulation.source-count:14}") int sourceCount,
            @Value("${simulation.signal-green-ratio:0.6}") double signalGreenRatio) {
        this.outRate = outRate;
        this.decay = decay;
        this.sourceRate = sourceRate;
        this.sourceCount = sourceCount;
        this.signalGreenRatio = signalGreenRatio;
    }

    @PostConstruct
    void load() {
        // Curated I1→I5 corridor (keeps its abstract nodes so routing still works).
        add(new Street("st-beira-rio", "Av. Hermann August Lepper (Beira-Rio)", "I1", "I5", 2000));
        add(new Street("st-joao-colin", "Rua Joao Colin", "I1", "I3", 1800));
        add(new Street("st-dona-francisca", "Rua Dona Francisca", "I3", "I5", 1400));
        add(new Street("st-nove-de-marco", "Rua Nove de Marco", "I1", "I2", 900));
        add(new Street("st-xv-de-novembro", "Rua XV de Novembro", "I2", "I3", 1000));

        // Real central-Joinville graph (capacity already derived from the OSM road class).
        Map<String, String> classById = new HashMap<>();
        for (Edge e : readEdges("/city-network.json")) {
            String name = (e.name() == null || e.name().isBlank()) ? "Rua sem nome" : e.name();
            Street s = new Street(e.id(), name, e.nodeA(), e.nodeB(), e.capacity());
            s.setOneway(e.oneway());
            classById.put(e.id(), e.roadClass());
            add(s);
        }

        seedDefaultSources(classById);
        applyTrafficSignals();
    }

    private void add(Street s) {
        network.addStreet(s);
    }

    /**
     * Seeds sources from the highest-capacity <em>through-roads</em> (motorways/arterials): those
     * are where traffic enters the city, so residential streets never spontaneously generate flow.
     */
    private void seedDefaultSources(Map<String, String> classById) {
        List<Street> candidates = new ArrayList<>();
        for (Street s : network.streets()) {
            String cls = classById.get(s.id());
            if (cls != null && SOURCE_CLASSES.contains(cls)) {
                candidates.add(s);
            }
        }
        candidates.sort((a, b) -> Integer.compare(b.hourlyCapacity(), a.hourlyCapacity()));
        int sources = Math.min(sourceCount, candidates.size());
        for (int i = 0; i < sources; i++) {
            candidates.get(i).setSource(true);
        }
    }

    /** Real OSM traffic signals: streets arriving at a signalised node lose throughput. */
    private void applyTrafficSignals() {
        Set<String> signalNodes = Set.of(readStrings("/traffic-signals.json"));
        for (Street s : network.streets()) {
            if (signalNodes.contains(s.toIntersectionId())) {
                s.addTrafficLight(signalGreenRatio);
            }
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

    private String[] readStrings(String resource) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                return new String[0];
            }
            return new ObjectMapper().readValue(in, String[].class);
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

    /**
     * Applies a partial topology edit (null fields are left unchanged) and marks the street so the
     * next SSE flush includes it even if its congestion level did not change. Thread-safe.
     */
    public synchronized void applyTopology(String id, Boolean oneway, Boolean blocked, Boolean source) {
        if (oneway != null) {
            network.setOneway(id, oneway);
        }
        if (blocked != null) {
            network.setBlocked(id, blocked);
        }
        if (source != null) {
            network.setSource(id, source);
        }
        if (network.get(id) != null) {
            topologyChanged.add(id);
        }
    }

    /** Returns and clears the streets edited since the last call (for the next SSE flush). */
    public synchronized List<Street> drainTopologyChanged() {
        List<Street> result = new ArrayList<>(topologyChanged.size());
        for (String id : topologyChanged) {
            Street s = network.get(id);
            if (s != null) {
                result.add(s);
            }
        }
        topologyChanged.clear();
        return result;
    }

    private record Edge(String id, String name, int capacity, String nodeA, String nodeB,
                        boolean oneway, @JsonProperty("class") String roadClass) { }
}
