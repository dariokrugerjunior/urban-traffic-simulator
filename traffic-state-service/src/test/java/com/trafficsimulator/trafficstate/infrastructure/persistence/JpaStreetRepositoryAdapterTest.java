package com.trafficsimulator.trafficstate.infrastructure.persistence;

import com.trafficsimulator.trafficstate.domain.model.Street;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(JpaStreetRepositoryAdapter.class)
class JpaStreetRepositoryAdapterTest {

    @Autowired
    JpaStreetRepositoryAdapter adapter;

    @Test
    void savesAndReloadsStreetWithTrafficLightState() {
        Street street = new Street("s1", "Main", "I1", "I2", 1000, 100);
        street.addTrafficLight(0.5);
        adapter.save(street);

        Street reloaded = adapter.findById("s1").orElseThrow();
        assertEquals(1, reloaded.trafficLightCount());
        assertEquals(500, reloaded.effectiveCapacity());
        assertEquals(100, reloaded.currentVolume());
    }

    @Test
    void findAllReturnsPersistedStreets() {
        adapter.save(new Street("s1", "A", "I1", "I2", 100));
        adapter.save(new Street("s2", "B", "I2", "I3", 100));
        assertEquals(2, adapter.findAll().size());
    }
}
