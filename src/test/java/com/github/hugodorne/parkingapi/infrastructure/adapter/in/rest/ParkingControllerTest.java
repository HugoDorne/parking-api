package com.github.hugodorne.parkingapi.infrastructure.adapter.in.rest;

import com.github.hugodorne.parkingapi.domain.model.Parking;
import com.github.hugodorne.parkingapi.domain.model.ParkingStatus;
import com.github.hugodorne.parkingapi.domain.port.in.GetParkingsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ParkingController
 */
@WebMvcTest(ParkingController.class)
class ParkingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetParkingsUseCase getParkingsUseCase;

    private List<Parking> mockParkings;

    @BeforeEach
    void setUp() {
        mockParkings = List.of(
                Parking.builder()
                        .id("1")
                        .name("Parking Centre")
                        .address("1 Rue du Centre")
                        .latitude(46.5802)
                        .longitude(0.3404)
                        .totalSpaces(100)
                        .availableSpaces(50)
                        .status(ParkingStatus.OPEN)
                        .build(),
                Parking.builder()
                        .id("2")
                        .name("Parking Gare")
                        .address("Place de la Gare")
                        .latitude(46.5835)
                        .longitude(0.3442)
                        .totalSpaces(200)
                        .availableSpaces(0)
                        .status(ParkingStatus.FULL)
                        .build()
        );
    }

    @Test
    void shouldReturnAllParkings() throws Exception {
        // Given
        when(getParkingsUseCase.getAllParkings()).thenReturn(mockParkings);

        // When & Then
        mockMvc.perform(get("/api/parkings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].name", is("Parking Centre")))
                .andExpect(jsonPath("$[0].address", is("1 Rue du Centre")))
                .andExpect(jsonPath("$[0].latitude", is(46.5802)))
                .andExpect(jsonPath("$[0].longitude", is(0.3404)))
                .andExpect(jsonPath("$[0].totalSpaces", is(100)))
                .andExpect(jsonPath("$[0].availableSpaces", is(50)))
                .andExpect(jsonPath("$[0].status", is("OPEN")))
                .andExpect(jsonPath("$[0].isOpen", is(true)))
                .andExpect(jsonPath("$[0].occupancyRate", is(50.0)))
                .andExpect(jsonPath("$[1].id", is("2")))
                .andExpect(jsonPath("$[1].status", is("FULL")))
                .andExpect(jsonPath("$[1].isOpen", is(false)));
    }

    @Test
    void shouldReturnEmptyListWhenNoParkingsAvailable() throws Exception {
        // Given
        when(getParkingsUseCase.getAllParkings()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/parkings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnParkingsNearbyWithDefaultRadius() throws Exception {
        // Given
        List<Parking> nearbyParkings = List.of(
                mockParkings.get(0).toBuilder().distanceKm(2.5).build()
        );

        when(getParkingsUseCase.getParkingsNearby(46.5802, 0.3404, 5.0))
                .thenReturn(nearbyParkings);

        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "46.5802")
                        .param("longitude", "0.3404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].distanceKm", is(2.5)));
    }

    @Test
    void shouldReturnParkingsNearbyWithCustomRadius() throws Exception {
        // Given
        List<Parking> nearbyParkings = List.of(
                mockParkings.get(0).toBuilder().distanceKm(0.8).build(),
                mockParkings.get(1).toBuilder().distanceKm(0.5).build()
        );

        when(getParkingsUseCase.getParkingsNearby(46.5802, 0.3404, 1.0))
                .thenReturn(nearbyParkings);

        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "46.5802")
                        .param("longitude", "0.3404")
                        .param("radius", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].distanceKm", is(0.8)))
                .andExpect(jsonPath("$[1].distanceKm", is(0.5)));
    }

    @Test
    void shouldReturnBadRequestWhenLatitudeIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "100.0") // Invalid: > 90
                        .param("longitude", "0.3404"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLatitudeIsTooLow() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "-100.0") // Invalid: < -90
                        .param("longitude", "0.3404"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLongitudeIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "46.5802")
                        .param("longitude", "200.0")) // Invalid: > 180
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLongitudeIsTooLow() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "46.5802")
                        .param("longitude", "-200.0")) // Invalid: < -180
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRadiusIsNegative() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "46.5802")
                        .param("longitude", "0.3404")
                        .param("radius", "-1.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRadiusIsZero() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "46.5802")
                        .param("longitude", "0.3404")
                        .param("radius", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLatitudeIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("longitude", "0.3404"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLongitudeIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "46.5802"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldExcludeNullFieldsInResponse() throws Exception {
        // Given
        Parking parkingWithNulls = Parking.builder()
                .id("3")
                .name("Minimal Parking")
                .status(ParkingStatus.UNKNOWN)
                .build();

        when(getParkingsUseCase.getAllParkings()).thenReturn(List.of(parkingWithNulls));

        // When & Then
        mockMvc.perform(get("/api/parkings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("3")))
                .andExpect(jsonPath("$[0].name", is("Minimal Parking")))
                .andExpect(jsonPath("$[0].address").doesNotExist())
                .andExpect(jsonPath("$[0].latitude").doesNotExist())
                .andExpect(jsonPath("$[0].longitude").doesNotExist())
                .andExpect(jsonPath("$[0].totalSpaces").doesNotExist())
                .andExpect(jsonPath("$[0].availableSpaces").doesNotExist());
    }

    @Test
    void shouldAcceptValidBoundaryLatitudeValues() throws Exception {
        // Given
        when(getParkingsUseCase.getParkingsNearby(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of());

        // When & Then - Test minimum latitude
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "-90.0")
                        .param("longitude", "0.0"))
                .andExpect(status().isOk());

        // Test maximum latitude
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "90.0")
                        .param("longitude", "0.0"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAcceptValidBoundaryLongitudeValues() throws Exception {
        // Given
        when(getParkingsUseCase.getParkingsNearby(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of());

        // When & Then - Test minimum longitude
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "0.0")
                        .param("longitude", "-180.0"))
                .andExpect(status().isOk());

        // Test maximum longitude
        mockMvc.perform(get("/api/parkings/nearby")
                        .param("latitude", "0.0")
                        .param("longitude", "180.0"))
                .andExpect(status().isOk());
    }
}

