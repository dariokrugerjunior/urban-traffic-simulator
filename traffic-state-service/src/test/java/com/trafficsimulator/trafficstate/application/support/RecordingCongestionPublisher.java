package com.trafficsimulator.trafficstate.application.support;

import com.trafficsimulator.trafficstate.domain.model.Street;
import com.trafficsimulator.trafficstate.domain.port.StreetCongestionPublisher;
import java.util.ArrayList;
import java.util.List;

public class RecordingCongestionPublisher implements StreetCongestionPublisher {
    public final List<Street> published = new ArrayList<>();
    public final List<Street> cleared = new ArrayList<>();

    @Override
    public void publishCongested(Street street) {
        published.add(street);
    }

    @Override
    public void publishCleared(Street street) {
        cleared.add(street);
    }
}
