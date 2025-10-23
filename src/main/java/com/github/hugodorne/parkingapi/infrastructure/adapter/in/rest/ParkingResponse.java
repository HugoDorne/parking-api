package com.github.hugodorne.parkingapi.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.hugodorne.parkingapi.domain.model.Parking;
import com.github.hugodorne.parkingapi.domain.model.ParkingStatus;
import lombok.Builder;
import lombok.Value;

/**
 * Response DTO for parking information
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingResponse {
    String id;
    String name;
    String address;
    Double latitude;
    Double longitude;
    Integer totalSpaces;
    Integer availableSpaces;
    ParkingStatus status;
    Boolean isOpen;
    Double occupancyRate;
    Double distanceKm;  // Distance from user position in kilometers (only for nearby queries)

    public static ParkingResponse fromDomain(Parking parking) {
        return ParkingResponse.builder()
                .id(parking.getId())
                .name(parking.getName())
                .address(parking.getAddress())
                .latitude(parking.getLatitude())
                .longitude(parking.getLongitude())
                .totalSpaces(parking.getTotalSpaces())
                .availableSpaces(parking.getAvailableSpaces())
                .status(parking.getStatus())
                .isOpen(parking.isOpen())
                .distanceKm(parking.getDistanceKm())
                .occupancyRate(parking.getOccupancyRate())
                .build();
    }
}

