package com.trafficsimulator.trafficstate.application.support;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryStreetRepository implements StreetRepository {
    private final Map<String, Street> store = new LinkedHashMap<>();

    public InMemoryStreetRepository(Street... seed) {
        for (Street s : seed) {
            store.put(s.id(), s);
        }
    }

    @Override
    public Optional<Street> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Street save(Street street) {
        store.put(street.id(), street);
        return street;
    }

    @Override
    public List<Street> findAll() {
        return new ArrayList<>(store.values());
    }
}
