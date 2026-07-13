package com.trafficsimulator.trafficstate.application;

public class StreetNotFoundException extends RuntimeException {
    public StreetNotFoundException(String streetId) {
        super("Street not found: " + streetId);
    }
}
