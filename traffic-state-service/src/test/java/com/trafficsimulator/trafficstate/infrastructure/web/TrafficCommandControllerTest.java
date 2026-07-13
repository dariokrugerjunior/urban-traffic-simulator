package com.trafficsimulator.trafficstate.infrastructure.web;

import com.trafficsimulator.trafficstate.application.GetTrafficSnapshotUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrafficCommandController.class)
class TrafficCommandControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean KafkaTemplate<String, Object> kafkaTemplate;
    @MockitoBean GetTrafficSnapshotUseCase snapshotUseCase;

    @Test
    void injectFlowPublishesEventAndReturns202() throws Exception {
        mvc.perform(post("/api/traffic/streets/st-beira-rio/flow")
                .contentType("application/json").content("{\"vehicles\":100}"))
           .andExpect(status().isAccepted());

        verify(kafkaTemplate).send(eq("flow-injected"), eq("st-beira-rio"), any());
    }

    @Test
    void addTrafficLightPublishesEventAndReturns202() throws Exception {
        mvc.perform(post("/api/traffic/streets/st-beira-rio/traffic-light")
                .contentType("application/json").content("{\"greenRatio\":0.5}"))
           .andExpect(status().isAccepted());

        verify(kafkaTemplate).send(eq("traffic-light-added"), eq("st-beira-rio"), any());
    }
}
