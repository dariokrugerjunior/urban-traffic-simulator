package com.trafficsimulator.trafficstate.infrastructure.web.dto;

public record AddTrafficLightRequest(Double greenRatio) {
    public double greenRatioOrDefault() {
        return greenRatio == null ? 0.5 : greenRatio;
    }
}
