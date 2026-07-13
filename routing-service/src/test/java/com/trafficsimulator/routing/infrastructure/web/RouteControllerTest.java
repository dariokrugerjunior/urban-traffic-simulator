package com.trafficsimulator.routing.infrastructure.web;

import com.trafficsimulator.routing.application.FindRouteUseCase;
import com.trafficsimulator.routing.application.GetNetworkStateUseCase;
import com.trafficsimulator.routing.domain.model.Route;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RouteController.class)
class RouteControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean FindRouteUseCase findRoute;
    @MockitoBean GetNetworkStateUseCase networkState;

    @Test
    void returnsRouteJson() throws Exception {
        when(findRoute.find("I1", "I5"))
            .thenReturn(new Route(List.of("st-beira-rio"), List.of("I1", "I5"), 5.0, true));

        mvc.perform(get("/api/routes").param("start", "I1").param("end", "I5"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.found").value(true))
           .andExpect(jsonPath("$.totalCost").value(5.0))
           .andExpect(jsonPath("$.streets[0]").value("st-beira-rio"));
    }
}
