package com.trafficsimulator.trafficstate.infrastructure.persistence;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaStreetRepositoryAdapter implements StreetRepository {

    private final SpringDataStreetRepository jpa;

    public JpaStreetRepositoryAdapter(SpringDataStreetRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Street> findById(String id) {
        return jpa.findById(id).map(StreetJpaMapper::toDomain);
    }

    @Override
    public Street save(Street street) {
        jpa.save(StreetJpaMapper.toEntity(street));
        return street;
    }

    @Override
    public List<Street> findAll() {
        return jpa.findAll().stream().map(StreetJpaMapper::toDomain).toList();
    }
}
