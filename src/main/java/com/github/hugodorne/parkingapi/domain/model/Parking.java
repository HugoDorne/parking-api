package com.github.hugodorne.parkingapi.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Domain model representing a parking facility
 */
@Value
@Builder(toBuilder = true)
public class Parking {
    String id;
    String name;
    String address;
    Double latitude;
    Double longitude;
    Integer totalSpaces;
    Integer availableSpaces;
    ParkingStatus status;
    Double distanceKm;  // Distance from user position in kilometers (optional)

    public boolean isOpen() {
        return status == ParkingStatus.OPEN;
    }

    public double getOccupancyRate() {
        if (totalSpaces == null || totalSpaces == 0) {
            return 0.0;
        }
        return Math.floor((double) (totalSpaces - (availableSpaces != null ? availableSpaces : 0)) * 100 / totalSpaces);
    }
}

