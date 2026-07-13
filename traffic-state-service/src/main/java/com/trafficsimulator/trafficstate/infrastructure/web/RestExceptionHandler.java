package com.trafficsimulator.trafficstate.infrastructure.web;

import com.trafficsimulator.trafficstate.application.StreetNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(StreetNotFoundException.class)
    public ResponseEntity<String> handleNotFound(StreetNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
