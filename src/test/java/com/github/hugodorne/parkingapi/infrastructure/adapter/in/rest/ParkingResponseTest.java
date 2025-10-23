package com.github.hugodorne.parkingapi.infrastructure.adapter.in.rest;

import com.github.hugodorne.parkingapi.domain.model.Parking;
import com.github.hugodorne.parkingapi.domain.model.ParkingStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ParkingResponse
 */
class ParkingResponseTest {

    @Test
    void shouldMapFromDomainCorrectly() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .address("123 Test Street")
                .latitude(46.5802)
                .longitude(0.3404)
                .totalSpaces(100)
                .availableSpaces(50)
                .status(ParkingStatus.OPEN)
                .distanceKm(2.5)
                .build();

        // When
        ParkingResponse response = ParkingResponse.fromDomain(parking);

        // Then
        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getName()).isEqualTo("Test Parking");
        assertThat(response.getAddress()).isEqualTo("123 Test Street");
        assertThat(response.getLatitude()).isEqualTo(46.5802);
        assertThat(response.getLongitude()).isEqualTo(0.3404);
        assertThat(response.getTotalSpaces()).isEqualTo(100);
        assertThat(response.getAvailableSpaces()).isEqualTo(50);
        assertThat(response.getStatus()).isEqualTo(ParkingStatus.OPEN);
        assertThat(response.getIsOpen()).isTrue();
        assertThat(response.getDistanceKm()).isEqualTo(2.5);
        assertThat(response.getOccupancyRate()).isEqualTo(50.0);
    }

    @Test
    void shouldMapClosedParkingCorrectly() {
        // Given
        Parking parking = Parking.builder()
                .id("2")
                .name("Closed Parking")
                .status(ParkingStatus.CLOSED)
                .totalSpaces(100)
                .availableSpaces(100)
                .build();

        // When
        ParkingResponse response = ParkingResponse.fromDomain(parking);

        // Then
        assertThat(response.getStatus()).isEqualTo(ParkingStatus.CLOSED);
        assertThat(response.getIsOpen()).isFalse();
    }

    @Test
    void shouldMapFullParkingCorrectly() {
        // Given
        Parking parking = Parking.builder()
                .id("3")
                .name("Full Parking")
                .status(ParkingStatus.FULL)
                .totalSpaces(100)
                .availableSpaces(0)
                .build();

        // When
        ParkingResponse response = ParkingResponse.fromDomain(parking);

        // Then
        assertThat(response.getStatus()).isEqualTo(ParkingStatus.FULL);
        assertThat(response.getIsOpen()).isFalse();
        assertThat(response.getOccupancyRate()).isEqualTo(100.0);
    }

    @Test
    void shouldHandleNullFieldsInDomain() {
        // Given
        Parking parking = Parking.builder()
                .id("4")
                .name("Minimal Parking")
                .status(ParkingStatus.UNKNOWN)
                .build();

        // When
        ParkingResponse response = ParkingResponse.fromDomain(parking);

        // Then
        assertThat(response.getId()).isEqualTo("4");
        assertThat(response.getName()).isEqualTo("Minimal Parking");
        assertThat(response.getAddress()).isNull();
        assertThat(response.getLatitude()).isNull();
        assertThat(response.getLongitude()).isNull();
        assertThat(response.getTotalSpaces()).isNull();
        assertThat(response.getAvailableSpaces()).isNull();
        assertThat(response.getDistanceKm()).isNull();
    }

    @Test
    void shouldIncludeDistanceForNearbyQueries() {
        // Given
        Parking parkingWithDistance = Parking.builder()
                .id("5")
                .name("Nearby Parking")
                .status(ParkingStatus.OPEN)
                .distanceKm(1.23)
                .totalSpaces(50)
                .availableSpaces(25)
                .build();

        // When
        ParkingResponse response = ParkingResponse.fromDomain(parkingWithDistance);

        // Then
        assertThat(response.getDistanceKm()).isEqualTo(1.23);
    }

    @Test
    void shouldNotIncludeDistanceWhenNotSet() {
        // Given
        Parking parkingWithoutDistance = Parking.builder()
                .id("6")
                .name("Regular Parking")
                .status(ParkingStatus.OPEN)
                .totalSpaces(50)
                .availableSpaces(25)
                .build();

        // When
        ParkingResponse response = ParkingResponse.fromDomain(parkingWithoutDistance);

        // Then
        assertThat(response.getDistanceKm()).isNull();
    }
}

