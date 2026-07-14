package com.trafficsimulator.trafficstate.infrastructure.simulation;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The live, in-memory {@link StreetRepository} backed by the {@link SimulationEngine}. Marked
 * {@code @Primary} so the use cases and the snapshot operate on the running simulation. The JPA
 * adapter remains as a separate (tested) persistence bean.
 */
@Repository
@Primary
public class EngineStreetRepository implements StreetRepository {

    private final SimulationEngine engine;

    public EngineStreetRepository(SimulationEngine engine) {
        this.engine = engine;
    }

    @Override
    public Optional<Street> findById(String id) {
        return engine.withNetwork(n -> Optional.ofNullable(n.get(id)));
    }

    @Override
    public Street save(Street street) {
        return engine.withNetwork(n -> {
            n.addStreet(street);
            return street;
        });
    }

    @Override
    public List<Street> findAll() {
        return engine.withNetwork(n -> new ArrayList<>(n.streets()));
    }
}
