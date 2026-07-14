package com.trafficsimulator.routing.infrastructure.web;

import com.trafficsimulator.routing.application.FindRouteBetweenStreetsUseCase;
import com.trafficsimulator.routing.application.FindRouteUseCase;
import com.trafficsimulator.routing.application.GetNetworkStateUseCase;
import com.trafficsimulator.routing.infrastructure.web.dto.RouteView;
import com.trafficsimulator.routing.infrastructure.web.dto.StreetWeightView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final FindRouteUseCase findRoute;
    private final FindRouteBetweenStreetsUseCase findRouteBetweenStreets;
    private final GetNetworkStateUseCase networkState;

    public RouteController(FindRouteUseCase findRoute,
                           FindRouteBetweenStreetsUseCase findRouteBetweenStreets,
                           GetNetworkStateUseCase networkState) {
        this.findRoute = findRoute;
        this.findRouteBetweenStreets = findRouteBetweenStreets;
        this.networkState = networkState;
    }

    @GetMapping
    public RouteView route(@RequestParam String start, @RequestParam String end) {
        return RouteView.from(findRoute.find(start, end));
    }

    @GetMapping("/between")
    public RouteView routeBetween(@RequestParam String fromStreet, @RequestParam String toStreet) {
        return RouteView.from(findRouteBetweenStreets.find(fromStreet, toStreet));
    }

    @GetMapping("/state")
    public List<StreetWeightView> state() {
        return networkState.streets().stream().map(StreetWeightView::from).toList();
    }
}
