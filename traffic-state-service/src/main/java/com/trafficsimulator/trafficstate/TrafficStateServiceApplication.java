package com.trafficsimulator.trafficstate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap entry point for the traffic-state-service.
 *
 * <p>Framework annotations are allowed here: this is the infrastructure/bootstrap
 * layer, not the domain. The {@code domain} package stays pure Java.
 */
@SpringBootApplication
public class TrafficStateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficStateServiceApplication.class, args);
    }
}
