package com.trafficsimulator.trafficstate.infrastructure.web.dto;

import com.trafficsimulator.trafficstate.domain.model.Street;

public record StreetStateView(String id, String name, int currentVolume, int effectiveCapacity,
                              double ratio, String congestionLevel, String color) {
    public static StreetStateView from(Street s) {
        return new StreetStateView(s.id(), s.name(), s.currentVolume(), s.effectiveCapacity(),
                s.congestionRatio(), s.congestionLevel().name(), s.congestionLevel().color());
    }
}
