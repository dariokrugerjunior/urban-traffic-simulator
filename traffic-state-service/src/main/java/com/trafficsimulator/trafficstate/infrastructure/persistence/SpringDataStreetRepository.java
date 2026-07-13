package com.trafficsimulator.trafficstate.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStreetRepository extends JpaRepository<StreetJpaEntity, String> { }
