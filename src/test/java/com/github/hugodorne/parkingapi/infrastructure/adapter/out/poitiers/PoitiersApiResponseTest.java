package com.github.hugodorne.parkingapi.infrastructure.adapter.out.poitiers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PoitiersApiResponse
 */
class PoitiersApiResponseTest {

    @Test
    void shouldCreateApiResponseWithData() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        response.setTotal(1);

        PoitiersApiResponse.ParkingData parkingData = new PoitiersApiResponse.ParkingData();
        parkingData.setId(1);
        parkingData.setNom("Test Parking");
        parkingData.setCapacite(100);
        parkingData.setPlaces(50);
        parkingData.setGeopoint("46.5802, 0.3404");
        parkingData.setTauxOccupation(50.0);
        parkingData.setDerniereMiseAJour("2025-10-23T10:00:00");

        response.setResults(java.util.List.of(parkingData));

        // When & Then
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getResults()).hasSize(1);

        PoitiersApiResponse.ParkingData data = response.getResults().get(0);
        assertThat(data.getId()).isEqualTo(1);
        assertThat(data.getNom()).isEqualTo("Test Parking");
        assertThat(data.getCapacite()).isEqualTo(100);
        assertThat(data.getPlaces()).isEqualTo(50);
        assertThat(data.getGeopoint()).isEqualTo("46.5802, 0.3404");
        assertThat(data.getTauxOccupation()).isEqualTo(50.0);
        assertThat(data.getDerniereMiseAJour()).isEqualTo("2025-10-23T10:00:00");
    }

    @Test
    void shouldHandleNullValues() {
        // Given
        PoitiersApiResponse.ParkingData parkingData = new PoitiersApiResponse.ParkingData();

        // When & Then
        assertThat(parkingData.getId()).isNull();
        assertThat(parkingData.getNom()).isNull();
        assertThat(parkingData.getCapacite()).isNull();
        assertThat(parkingData.getPlaces()).isNull();
        assertThat(parkingData.getGeopoint()).isNull();
        assertThat(parkingData.getTauxOccupation()).isNull();
        assertThat(parkingData.getInfoParkingsGeoPoint()).isNull();
        assertThat(parkingData.getDerniereMiseAJour()).isNull();
    }

    @Test
    void shouldSetAndGetAllFields() {
        // Given
        PoitiersApiResponse.ParkingData parkingData = new PoitiersApiResponse.ParkingData();

        // When
        parkingData.setId(42);
        parkingData.setNom("Parking Test");
        parkingData.setCapacite(150);
        parkingData.setPlaces(75);
        parkingData.setGeopoint("46.5802, 0.3404");
        parkingData.setInfoParkingsGeoPoint("46.5835, 0.3442");
        parkingData.setTauxOccupation(50.0);
        parkingData.setDerniereMiseAJour("2025-10-23");

        // Then
        assertThat(parkingData.getId()).isEqualTo(42);
        assertThat(parkingData.getNom()).isEqualTo("Parking Test");
        assertThat(parkingData.getCapacite()).isEqualTo(150);
        assertThat(parkingData.getPlaces()).isEqualTo(75);
        assertThat(parkingData.getGeopoint()).isEqualTo("46.5802, 0.3404");
        assertThat(parkingData.getInfoParkingsGeoPoint()).isEqualTo("46.5835, 0.3442");
        assertThat(parkingData.getTauxOccupation()).isEqualTo(50.0);
        assertThat(parkingData.getDerniereMiseAJour()).isEqualTo("2025-10-23");
    }
}

