package com.trafficsimulator.trafficstate.application.support;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.TrafficStateBroadcaster;
import java.util.ArrayList;
import java.util.List;

public class RecordingBroadcaster implements TrafficStateBroadcaster {
    public final List<Street> broadcasts = new ArrayList<>();

    @Override
    public void broadcast(Street street) {
        broadcasts.add(street);
    }
}
