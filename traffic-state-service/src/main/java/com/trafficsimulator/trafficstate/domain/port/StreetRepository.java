package com.trafficsimulator.trafficstate.domain.port;

import com.trafficsimulator.trafficstate.domain.model.Street;
import java.util.List;
import java.util.Optional;

/** Outbound port for persisting and loading streets. */
public interface StreetRepository {
    Optional<Street> findById(String id);
    Street save(Street street);
    List<Street> findAll();
}
