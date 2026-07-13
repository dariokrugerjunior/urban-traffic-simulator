package com.trafficsimulator.trafficstate.infrastructure.persistence;

import com.trafficsimulator.trafficstate.domain.model.Street;

final class StreetJpaMapper {
    private StreetJpaMapper() { }

    static Street toDomain(StreetJpaEntity e) {
        return new Street(e.getId(), e.getName(), e.getFromIntersectionId(), e.getToIntersectionId(),
                e.getHourlyCapacity(), e.getCurrentVolume(), e.getTrafficLightCount(), e.getGreenRatio());
    }

    static StreetJpaEntity toEntity(Street s) {
        return new StreetJpaEntity(s.id(), s.name(), s.fromIntersectionId(), s.toIntersectionId(),
                s.hourlyCapacity(), s.currentVolume(), s.trafficLightCount(), s.greenRatio());
    }
}
