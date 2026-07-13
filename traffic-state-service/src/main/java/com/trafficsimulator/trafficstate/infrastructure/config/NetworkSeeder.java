package com.trafficsimulator.trafficstate.infrastructure.config;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
            new Street("st-xv-de-novembro", "Rua XV de Novembro", "I2", "I3", 1000)
        );
        streets.forEach(repository::save);
    }
}
