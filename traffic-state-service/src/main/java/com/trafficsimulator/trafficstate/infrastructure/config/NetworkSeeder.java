package com.trafficsimulator.trafficstate.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/** Seeds the Joinville road network on startup (idempotent) so the MVP boots ready to use. */
@Component
public class NetworkSeeder implements CommandLineRunner {

    private final StreetRepository repository;

    public NetworkSeeder(StreetRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (!repository.findAll().isEmpty()) {
            return;
        }
        // The curated I1→I5 corridor keeps its abstract node ids so routing still works.
        List<Street> corridor = List.of(
            new Street("st-beira-rio", "Av. Hermann August Lepper (Beira-Rio)", "I1", "I5", 2000),
            new Street("st-joao-colin", "Rua Joao Colin", "I1", "I3", 1800),
            new Street("st-dona-francisca", "Rua Dona Francisca", "I3", "I5", 1400),
            new Street("st-nove-de-marco", "Rua Nove de Marco", "I1", "I2", 900),
            new Street("st-xv-de-novembro", "Rua XV de Novembro", "I2", "I3", 1000)
        );
        corridor.forEach(repository::save);

        // Bulk-seed the real central-Joinville graph (edges between real OSM intersections).
        seedFromResource("/city-network.json");
    }

    private void seedFromResource(String resource) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                return;
            }
            SeedStreet[] rows = new ObjectMapper().readValue(in, SeedStreet[].class);
            for (SeedStreet row : rows) {
                String name = (row.name() == null || row.name().isBlank()) ? "Rua sem nome" : row.name();
                repository.save(new Street(row.id(), name, row.nodeA(), row.nodeB(), row.capacity()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to seed streets from " + resource, e);
        }
    }

    /** One edge of the road graph. `oneway` is carried for later phases (topology metadata). */
    private record SeedStreet(String id, String name, int capacity, String nodeA, String nodeB, boolean oneway) { }
}
