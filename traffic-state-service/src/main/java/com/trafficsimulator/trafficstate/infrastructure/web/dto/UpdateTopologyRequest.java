package com.trafficsimulator.trafficstate.infrastructure.web.dto;

/**
 * Partial topology edit for a street. Any field may be null, meaning "leave unchanged" —
 * so the UI can send a single toggle (e.g. {@code {"blocked": true}}).
 */
public record UpdateTopologyRequest(Boolean oneway, Boolean blocked, Boolean source) { }
