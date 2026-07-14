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
        List<Street> streets = List.of(
            new Street("st-beira-rio", "Av. Hermann August Lepper (Beira-Rio)", "I1", "I5", 2000),
            new Street("st-joao-colin", "Rua Joao Colin", "I1", "I3", 1800),
            new Street("st-dona-francisca", "Rua Dona Francisca", "I3", "I5", 1400),
            new Street("st-nove-de-marco", "Rua Nove de Marco", "I1", "I2", 900),
            new Street("st-xv-de-novembro", "Rua XV de Novembro", "I2", "I3", 1000),
            // Additional arterials (nominal endpoints; simulated for congestion only).
            new Street("st-benjamin-constant", "Rua Benjamin Constant", "st-benjamin-constant-a", "st-benjamin-constant-b", 1200),
            new Street("st-iririu", "Rua Iririu", "st-iririu-a", "st-iririu-b", 1600),
            new Street("st-ottokar-doerffel", "Rua Ottokar Doerffel", "st-ottokar-doerffel-a", "st-ottokar-doerffel-b", 1600),
            new Street("st-guanabara", "Rua Guanabara", "st-guanabara-a", "st-guanabara-b", 1200),
            new Street("st-marques-de-olinda", "Rua Marques de Olinda", "st-marques-de-olinda-a", "st-marques-de-olinda-b", 1600),
            new Street("st-rui-barbosa", "Rua Rui Barbosa", "st-rui-barbosa-a", "st-rui-barbosa-b", 1200),
            new Street("st-tuiuti", "Rua Tuiuti", "st-tuiuti-a", "st-tuiuti-b", 1600),
            new Street("st-coronel-procopio-gomes", "Rua Coronel Procopio Gomes", "st-coronel-procopio-gomes-a", "st-coronel-procopio-gomes-b", 1600),
            new Street("st-hans-dieter-schmidt", "Rua Hans Dieter Schmidt", "st-hans-dieter-schmidt-a", "st-hans-dieter-schmidt-b", 1600),
            new Street("st-jose-vieira", "Avenida Jose Vieira", "st-jose-vieira-a", "st-jose-vieira-b", 1600),
            new Street("st-victor-schopping", "Avenida Victor Schopping", "st-victor-schopping-a", "st-victor-schopping-b", 1200),
            new Street("st-albano-schmidt", "Rua Albano Schmidt", "st-albano-schmidt-a", "st-albano-schmidt-b", 1600),
            new Street("st-florianopolis", "Rua Florianopolis", "st-florianopolis-a", "st-florianopolis-b", 1600),
            new Street("st-visconde-de-taunay", "Rua Visconde de Taunay", "st-visconde-de-taunay-a", "st-visconde-de-taunay-b", 1600),
            new Street("st-juscelino-kubitschek", "Avenida Juscelino Kubitschek", "st-juscelino-kubitschek-a", "st-juscelino-kubitschek-b", 1600),
            new Street("st-anita-garibaldi", "Rua Anita Garibaldi", "st-anita-garibaldi-a", "st-anita-garibaldi-b", 1600),
            new Street("st-marechal-hermes", "Rua Marechal Hermes", "st-marechal-hermes-a", "st-marechal-hermes-b", 900),
            new Street("st-aube", "Rua Aube", "st-aube-a", "st-aube-b", 1600)
        );
        streets.forEach(repository::save);

        // Bulk-seed the full Vila Nova neighborhood from a bundled resource.
        seedFromResource("/vila-nova-streets.json");
    }

    private void seedFromResource(String resource) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                return;
            }
            SeedStreet[] rows = new ObjectMapper().readValue(in, SeedStreet[].class);
            for (SeedStreet row : rows) {
                String name = (row.name() == null || row.name().isBlank()) ? "Rua sem nome" : row.name();
                repository.save(new Street(row.id(), name, row.id() + "-a", row.id() + "-b", row.capacity()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to seed streets from " + resource, e);
        }
    }

    private record SeedStreet(String id, String name, int capacity) { }
}
