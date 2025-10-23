package com.github.hugodorne.parkingapi.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response for REST endpoints
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    LocalDateTime timestamp;
    int status;
    String error;
    String message;
    Map<String, String> errors;
}

