package com.github.hugodorne.parkingapi.domain.service;

import com.github.hugodorne.parkingapi.domain.model.Parking;
import com.github.hugodorne.parkingapi.domain.model.ParkingStatus;
import com.github.hugodorne.parkingapi.domain.port.out.ParkingDataPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ParkingService
 */
@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private ParkingDataPort parkingDataPort;

    @InjectMocks
    private ParkingService parkingService;

    private List<Parking> mockParkings;

    @BeforeEach
    void setUp() {
        mockParkings = List.of(
                Parking.builder()
                        .id("1")
                        .name("Parking Centre")
                        .latitude(46.5802)
                        .longitude(0.3404)
                        .totalSpaces(100)
                        .availableSpaces(50)
                        .status(ParkingStatus.OPEN)
                        .build(),
                Parking.builder()
                        .id("2")
                        .name("Parking Gare")
                        .latitude(46.5835)
                        .longitude(0.3442)
                        .totalSpaces(200)
                        .availableSpaces(100)
                        .status(ParkingStatus.OPEN)
                        .build(),
                Parking.builder()
                        .id("3")
                        .name("Parking Nord")
                        .latitude(46.6000)
                        .longitude(0.3500)
                        .totalSpaces(150)
                        .availableSpaces(0)
                        .status(ParkingStatus.FULL)
                        .build(),
                Parking.builder()
                        .id("4")
                        .name("Parking Sans Coordonnées")
                        .latitude(null)
                        .longitude(null)
                        .totalSpaces(50)
                        .availableSpaces(25)
                        .status(ParkingStatus.OPEN)
                        .build()
        );
    }

    @Test
    void shouldReturnAllParkingsFromDataPort() {
        // Given
        when(parkingDataPort.fetchParkings()).thenReturn(mockParkings);

        // When
        List<Parking> result = parkingService.getAllParkings();

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).isEqualTo(mockParkings);
        verify(parkingDataPort, times(1)).fetchParkings();
    }

    @Test
    void shouldReturnEmptyListWhenNoDataAvailable() {
        // Given
        when(parkingDataPort.fetchParkings()).thenReturn(List.of());

        // When
        List<Parking> result = parkingService.getAllParkings();

        // Then
        assertThat(result).isEmpty();
        verify(parkingDataPort, times(1)).fetchParkings();
    }

    @Test
    void shouldReturnParkingsNearbyWithinRadius() {
        // Given
        double userLat = 46.5802;
        double userLon = 0.3404;
        double radiusKm = 5.0;

        when(parkingDataPort.fetchParkings()).thenReturn(mockParkings);

        // When
        List<Parking> result = parkingService.getParkingsNearby(userLat, userLon, radiusKm);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(p -> p.getDistanceKm() != null);
        assertThat(result).allMatch(p -> p.getDistanceKm() <= radiusKm);
        verify(parkingDataPort, times(1)).fetchParkings();
    }

    @Test
    void shouldFilterOutParkingsWithoutCoordinates() {
        // Given
        double userLat = 46.5802;
        double userLon = 0.3404;
        double radiusKm = 10.0;

        when(parkingDataPort.fetchParkings()).thenReturn(mockParkings);

        // When
        List<Parking> result = parkingService.getParkingsNearby(userLat, userLon, radiusKm);

        // Then
        assertThat(result).noneMatch(p -> p.getLatitude() == null || p.getLongitude() == null);
        assertThat(result).noneMatch(p -> p.getId().equals("4")); // Parking without coordinates
    }

    @Test
    void shouldSortParkingsByDistance() {
        // Given
        double userLat = 46.5802;
        double userLon = 0.3404;
        double radiusKm = 10.0;

        when(parkingDataPort.fetchParkings()).thenReturn(mockParkings);

        // When
        List<Parking> result = parkingService.getParkingsNearby(userLat, userLon, radiusKm);

        // Then
        assertThat(result).isNotEmpty();
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).getDistanceKm())
                    .isLessThanOrEqualTo(result.get(i + 1).getDistanceKm());
        }
    }

    @Test
    void shouldRoundDistanceToTwoDecimalPlaces() {
        // Given
        double userLat = 46.5802;
        double userLon = 0.3404;
        double radiusKm = 10.0;

        when(parkingDataPort.fetchParkings()).thenReturn(mockParkings);

        // When
        List<Parking> result = parkingService.getParkingsNearby(userLat, userLon, radiusKm);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(p -> {
            String distanceStr = String.valueOf(p.getDistanceKm());
            int decimalIndex = distanceStr.indexOf('.');
            if (decimalIndex == -1) return true;
            return distanceStr.substring(decimalIndex + 1).length() <= 2;
        });
    }

    @Test
    void shouldExcludeParkingsBeyondRadius() {
        // Given
        double userLat = 46.5802;
        double userLon = 0.3404;
        double radiusKm = 0.5; // Very small radius

        when(parkingDataPort.fetchParkings()).thenReturn(mockParkings);

        // When
        List<Parking> result = parkingService.getParkingsNearby(userLat, userLon, radiusKm);

        // Then
        assertThat(result).allMatch(p -> p.getDistanceKm() <= radiusKm);
    }

    @Test
    void shouldReturnEmptyListWhenNoParkingsWithinRadius() {
        // Given
        double userLat = 0.0;
        double userLon = 0.0;
        double radiusKm = 0.1;

        when(parkingDataPort.fetchParkings()).thenReturn(mockParkings);

        // When
        List<Parking> result = parkingService.getParkingsNearby(userLat, userLon, radiusKm);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCalculateDistanceCorrectlyUsingHaversineFormula() {
        // Given - Poitiers coordinates
        double userLat = 46.5802;
        double userLon = 0.3404;
        double radiusKm = 10.0;

        Parking nearbyParking = Parking.builder()
                .id("5")
                .name("Very Close Parking")
                .latitude(46.5802) // Same location
                .longitude(0.3404)
                .status(ParkingStatus.OPEN)
                .build();

        when(parkingDataPort.fetchParkings()).thenReturn(List.of(nearbyParking));

        // When
        List<Parking> result = parkingService.getParkingsNearby(userLat, userLon, radiusKm);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDistanceKm()).isEqualTo(0.0);
    }

    @Test
    void shouldHandleLargeParkingList() {
        // Given
        List<Parking> largeParkingList = List.of(
                createParkingAt("1", 46.5802, 0.3404),
                createParkingAt("2", 46.5835, 0.3442),
                createParkingAt("3", 46.6000, 0.3500),
                createParkingAt("4", 46.5900, 0.3600),
                createParkingAt("5", 46.5700, 0.3300)
        );

        when(parkingDataPort.fetchParkings()).thenReturn(largeParkingList);

        // When
        List<Parking> result = parkingService.getParkingsNearby(46.5802, 0.3404, 5.0);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(p -> p.getDistanceKm() != null);
    }

    private Parking createParkingAt(String id, double lat, double lon) {
        return Parking.builder()
                .id(id)
                .name("Parking " + id)
                .latitude(lat)
                .longitude(lon)
                .totalSpaces(100)
                .availableSpaces(50)
                .status(ParkingStatus.OPEN)
                .build();
    }
}

