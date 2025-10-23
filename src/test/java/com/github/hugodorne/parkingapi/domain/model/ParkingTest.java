package com.github.hugodorne.parkingapi.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Parking domain model
 */
class ParkingTest {

    @Test
    void shouldReturnTrueWhenParkingIsOpen() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .status(ParkingStatus.OPEN)
                .build();

        // When & Then
        assertThat(parking.isOpen()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenParkingIsClosed() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .status(ParkingStatus.CLOSED)
                .build();

        // When & Then
        assertThat(parking.isOpen()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenParkingIsFull() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .status(ParkingStatus.FULL)
                .build();

        // When & Then
        assertThat(parking.isOpen()).isFalse();
    }

    @Test
    void shouldCalculateCorrectOccupancyRateWhenParkingIsPartiallyFull() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .totalSpaces(100)
                .availableSpaces(30)
                .build();

        // When
        double occupancyRate = parking.getOccupancyRate();

        // Then
        assertThat(occupancyRate).isEqualTo(70.0);
    }

    @Test
    void shouldCalculateCorrectOccupancyRateWhenParkingIsEmpty() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .totalSpaces(100)
                .availableSpaces(100)
                .build();

        // When
        double occupancyRate = parking.getOccupancyRate();

        // Then
        assertThat(occupancyRate).isEqualTo(0.0);
    }

    @Test
    void shouldCalculateCorrectOccupancyRateWhenParkingIsFull() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .totalSpaces(100)
                .availableSpaces(0)
                .build();

        // When
        double occupancyRate = parking.getOccupancyRate();

        // Then
        assertThat(occupancyRate).isEqualTo(100.0);
    }

    @Test
    void shouldReturnZeroOccupancyRateWhenTotalSpacesIsNull() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .totalSpaces(null)
                .availableSpaces(10)
                .build();

        // When
        double occupancyRate = parking.getOccupancyRate();

        // Then
        assertThat(occupancyRate).isEqualTo(0.0);
    }

    @Test
    void shouldReturnZeroOccupancyRateWhenTotalSpacesIsZero() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .totalSpaces(0)
                .availableSpaces(0)
                .build();

        // When
        double occupancyRate = parking.getOccupancyRate();

        // Then
        assertThat(occupancyRate).isEqualTo(0.0);
    }

    @Test
    void shouldHandleNullAvailableSpacesAsZero() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .totalSpaces(100)
                .availableSpaces(null)
                .build();

        // When
        double occupancyRate = parking.getOccupancyRate();

        // Then
        assertThat(occupancyRate).isEqualTo(100.0);
    }

    @Test
    void shouldCreateParkingWithToBuilder() {
        // Given
        Parking original = Parking.builder()
                .id("1")
                .name("Original Parking")
                .totalSpaces(100)
                .availableSpaces(50)
                .status(ParkingStatus.OPEN)
                .build();

        // When
        Parking modified = original.toBuilder()
                .distanceKm(2.5)
                .build();

        // Then
        assertThat(modified.getId()).isEqualTo("1");
        assertThat(modified.getName()).isEqualTo("Original Parking");
        assertThat(modified.getDistanceKm()).isEqualTo(2.5);
    }

    @Test
    void shouldRoundOccupancyRateCorrectly() {
        // Given
        Parking parking = Parking.builder()
                .id("1")
                .name("Test Parking")
                .totalSpaces(3)
                .availableSpaces(1)
                .build();

        // When
        double occupancyRate = parking.getOccupancyRate();

        // Then - (3-1)*100/3 = 66.666... should be floored to 66.0
        assertThat(occupancyRate).isEqualTo(66.0);
    }
}

