package com.trafficsimulator.routing.infrastructure.web.dto;

import com.trafficsimulator.routing.domain.model.RouteStreet;

public record StreetWeightView(String id, String name, String from, String to,
                               double baseWeight, double penaltyFactor, double weight) {
    public static StreetWeightView from(RouteStreet s) {
        return new StreetWeightView(s.id(), s.name(), s.from(), s.to(),
                s.baseWeight(), s.penaltyFactor(), s.weight());
    }
}
