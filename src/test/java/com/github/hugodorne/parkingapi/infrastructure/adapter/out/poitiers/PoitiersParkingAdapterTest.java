package com.github.hugodorne.parkingapi.infrastructure.adapter.out.poitiers;

import com.github.hugodorne.parkingapi.domain.model.Parking;
import com.github.hugodorne.parkingapi.domain.model.ParkingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PoitiersParkingAdapter
 */
@ExtendWith(MockitoExtension.class)
class PoitiersParkingAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PoitiersParkingProperties properties;

    @InjectMocks
    private PoitiersParkingAdapter adapter;

    private String apiUrl;

    @BeforeEach
    void setUp() {
        apiUrl = "https://data.grandpoitiers.fr/data-fair/api/v1/datasets/mobilites-stationnement-des-parkings-en-temps-reel/lines";
        when(properties.getUrl()).thenReturn(apiUrl);
    }

    @Test
    void shouldFetchAndMapParkingsCorrectly() {
        // Given
        PoitiersApiResponse response = createMockApiResponse();
        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(2);

        Parking parking1 = parkings.get(0);
        assertThat(parking1.getId()).isEqualTo("1");
        assertThat(parking1.getName()).isEqualTo("Parking Centre");
        assertThat(parking1.getLatitude()).isEqualTo(46.5802);
        assertThat(parking1.getLongitude()).isEqualTo(0.3404);
        assertThat(parking1.getTotalSpaces()).isEqualTo(100);
        assertThat(parking1.getAvailableSpaces()).isEqualTo(50);
        assertThat(parking1.getStatus()).isEqualTo(ParkingStatus.OPEN);

        verify(restTemplate, times(1)).getForObject(apiUrl, PoitiersApiResponse.class);
    }

    @Test
    void shouldReturnEmptyListWhenApiReturnsNull() {
        // Given
        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(null);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenApiReturnsNullResults() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        response.setTotal(0);
        response.setResults(null);

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenApiThrowsException() {
        // Given
        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenThrow(new RestClientException("Connection error"));

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).isEmpty();
    }

    @Test
    void shouldHandleParkingWithoutGeopoint() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        PoitiersApiResponse.ParkingData data = new PoitiersApiResponse.ParkingData();
        data.setId(1);
        data.setNom("Parking Sans Coordonnées");
        data.setCapacite(50);
        data.setPlaces(25);
        data.setGeopoint(null);
        data.setInfoParkingsGeoPoint(null);

        response.setTotal(1);
        response.setResults(List.of(data));

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(1);
        assertThat(parkings.get(0).getLatitude()).isNull();
        assertThat(parkings.get(0).getLongitude()).isNull();
    }

    @Test
    void shouldUseAlternativeGeopointField() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        PoitiersApiResponse.ParkingData data = new PoitiersApiResponse.ParkingData();
        data.setId(1);
        data.setNom("Parking Test");
        data.setCapacite(100);
        data.setPlaces(50);
        data.setGeopoint(null);
        data.setInfoParkingsGeoPoint("46.5835, 0.3442");

        response.setTotal(1);
        response.setResults(List.of(data));

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(1);
        assertThat(parkings.get(0).getLatitude()).isEqualTo(46.5835);
        assertThat(parkings.get(0).getLongitude()).isEqualTo(0.3442);
    }

    @Test
    void shouldHandleInvalidGeopointFormat() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        PoitiersApiResponse.ParkingData data = new PoitiersApiResponse.ParkingData();
        data.setId(1);
        data.setNom("Parking Test");
        data.setCapacite(100);
        data.setPlaces(50);
        data.setGeopoint("invalid_format");

        response.setTotal(1);
        response.setResults(List.of(data));

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(1);
        assertThat(parkings.get(0).getLatitude()).isNull();
        assertThat(parkings.get(0).getLongitude()).isNull();
    }

    @Test
    void shouldDetermineStatusAsFullWhenNoSpacesAvailable() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        PoitiersApiResponse.ParkingData data = new PoitiersApiResponse.ParkingData();
        data.setId(1);
        data.setNom("Parking Complet");
        data.setCapacite(100);
        data.setPlaces(0);
        data.setGeopoint("46.5802, 0.3404");

        response.setTotal(1);
        response.setResults(List.of(data));

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(1);
        assertThat(parkings.get(0).getStatus()).isEqualTo(ParkingStatus.FULL);
    }

    @Test
    void shouldDetermineStatusAsOpenWhenSpacesAvailable() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        PoitiersApiResponse.ParkingData data = new PoitiersApiResponse.ParkingData();
        data.setId(1);
        data.setNom("Parking Ouvert");
        data.setCapacite(100);
        data.setPlaces(50);
        data.setGeopoint("46.5802, 0.3404");

        response.setTotal(1);
        response.setResults(List.of(data));

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(1);
        assertThat(parkings.get(0).getStatus()).isEqualTo(ParkingStatus.OPEN);
    }

    @Test
    void shouldDetermineStatusAsUnknownWhenDataIsNull() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        PoitiersApiResponse.ParkingData data = new PoitiersApiResponse.ParkingData();
        data.setId(1);
        data.setNom("Parking Inconnu");
        data.setCapacite(null);
        data.setPlaces(null);
        data.setGeopoint("46.5802, 0.3404");

        response.setTotal(1);
        response.setResults(List.of(data));

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(1);
        assertThat(parkings.get(0).getStatus()).isEqualTo(ParkingStatus.UNKNOWN);
    }

    @Test
    void shouldUseNameAsIdWhenIdIsNull() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        PoitiersApiResponse.ParkingData data = new PoitiersApiResponse.ParkingData();
        data.setId(null);
        data.setNom("Parking Sans ID");
        data.setCapacite(100);
        data.setPlaces(50);
        data.setGeopoint("46.5802, 0.3404");

        response.setTotal(1);
        response.setResults(List.of(data));

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(1);
        assertThat(parkings.get(0).getId()).isEqualTo("Parking Sans ID");
    }

    @Test
    void shouldHandleEmptyGeopointString() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        PoitiersApiResponse.ParkingData data = new PoitiersApiResponse.ParkingData();
        data.setId(1);
        data.setNom("Parking Test");
        data.setCapacite(100);
        data.setPlaces(50);
        data.setGeopoint("");

        response.setTotal(1);
        response.setResults(List.of(data));

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(1);
        assertThat(parkings.get(0).getLatitude()).isNull();
        assertThat(parkings.get(0).getLongitude()).isNull();
    }

    @Test
    void shouldHandleGeopointWithOnlyOneCoordinate() {
        // Given
        PoitiersApiResponse response = new PoitiersApiResponse();
        PoitiersApiResponse.ParkingData data = new PoitiersApiResponse.ParkingData();
        data.setId(1);
        data.setNom("Parking Test");
        data.setCapacite(100);
        data.setPlaces(50);
        data.setGeopoint("46.5802");

        response.setTotal(1);
        response.setResults(List.of(data));

        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(1);
        assertThat(parkings.get(0).getLatitude()).isNull();
        assertThat(parkings.get(0).getLongitude()).isNull();
    }

    @Test
    void shouldParseRealPoitiersApiDataCorrectly() {
        // Given - Real data from Poitiers API
        PoitiersApiResponse response = createRealPoitiersApiResponse();
        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        assertThat(parkings).hasSize(8);

        // Verify THEATRE parking
        Parking theatre = parkings.stream()
                .filter(p -> "THEATRE".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(theatre.getId()).isEqualTo("3");
        assertThat(theatre.getTotalSpaces()).isEqualTo(320);
        assertThat(theatre.getAvailableSpaces()).isEqualTo(32);
        assertThat(theatre.getLatitude()).isEqualTo(46.58383455409422);
        assertThat(theatre.getLongitude()).isEqualTo(0.33779491061805567);
        assertThat(theatre.getStatus()).isEqualTo(ParkingStatus.OPEN);

        // Verify PALAIS DE JUSTICE parking
        Parking palais = parkings.stream()
                .filter(p -> "PALAIS DE JUSTICE".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(palais.getId()).isEqualTo("12");
        assertThat(palais.getTotalSpaces()).isEqualTo(228);
        assertThat(palais.getAvailableSpaces()).isEqualTo(130);
        assertThat(palais.getLatitude()).isEqualTo(46.58595804860371);
        assertThat(palais.getLongitude()).isEqualTo(0.3512954265806957);
        assertThat(palais.getStatus()).isEqualTo(ParkingStatus.OPEN);

        // Verify HOTEL DE VILLE parking
        Parking hotelVille = parkings.stream()
                .filter(p -> "HOTEL DE VILLE".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(hotelVille.getId()).isEqualTo("2");
        assertThat(hotelVille.getTotalSpaces()).isEqualTo(625);
        assertThat(hotelVille.getAvailableSpaces()).isEqualTo(273);
        assertThat(hotelVille.getStatus()).isEqualTo(ParkingStatus.OPEN);
    }

    @Test
    void shouldHandleParkingsWithoutGeopointFromRealData() {
        // Given - Real data includes parkings without geopoint (GARE EFFIA, CORDELIERS)
        PoitiersApiResponse response = createRealPoitiersApiResponse();
        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then
        Parking gareEffia = parkings.stream()
                .filter(p -> "GARE EFFIA".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(gareEffia.getId()).isEqualTo("5");
        assertThat(gareEffia.getTotalSpaces()).isEqualTo(480);
        assertThat(gareEffia.getAvailableSpaces()).isEqualTo(244);
        assertThat(gareEffia.getLatitude()).isNull();
        assertThat(gareEffia.getLongitude()).isNull();
        assertThat(gareEffia.getStatus()).isEqualTo(ParkingStatus.OPEN);

        Parking cordeliers = parkings.stream()
                .filter(p -> "CORDELIERS".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(cordeliers.getId()).isEqualTo("6");
        assertThat(cordeliers.getLatitude()).isNull();
        assertThat(cordeliers.getLongitude()).isNull();
    }

    @Test
    void shouldCalculateOccupancyFromRealData() {
        // Given
        PoitiersApiResponse response = createRealPoitiersApiResponse();
        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then - Verify occupancy calculation
        Parking theatre = parkings.stream()
                .filter(p -> "THEATRE".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        // THEATRE: 320 total, 32 available = (320-32)*100/320 = 90%
        assertThat(theatre.getOccupancyRate()).isEqualTo(90.0);

        Parking blossac = parkings.stream()
                .filter(p -> "BLOSSAC TISON".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        // BLOSSAC TISON: 665 total, 375 available = (665-375)*100/665 = 43.60%
        assertThat(blossac.getOccupancyRate()).isEqualTo(43.0); // Floor value
    }

    @Test
    void shouldUseInfoParkingsGeoPointFieldFromRealData() {
        // Given - Real data uses "infos_parkingsgeo_point" field
        PoitiersApiResponse response = createRealPoitiersApiResponse();
        when(restTemplate.getForObject(apiUrl, PoitiersApiResponse.class))
                .thenReturn(response);

        // When
        List<Parking> parkings = adapter.fetchParkings();

        // Then - All parkings with coordinates should be parsed correctly
        long parkingsWithCoordinates = parkings.stream()
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null)
                .count();
        assertThat(parkingsWithCoordinates).isEqualTo(6); // 6 out of 8 have coordinates
    }

    private PoitiersApiResponse createRealPoitiersApiResponse() {
        PoitiersApiResponse response = new PoitiersApiResponse();
        response.setTotal(8);

        PoitiersApiResponse.ParkingData theatre = new PoitiersApiResponse.ParkingData();
        theatre.setId(3);
        theatre.setNom("THEATRE");
        theatre.setCapacite(320);
        theatre.setPlaces(32);
        theatre.setGeopoint("46.58383455409422, 0.33779491061805567");
        theatre.setInfoParkingsGeoPoint("46.58383455409422, 0.33779491061805567");
        theatre.setTauxOccupation(90.0);
        theatre.setDerniereMiseAJour("2025-10-23T11:21:00+02:00");

        PoitiersApiResponse.ParkingData palais = new PoitiersApiResponse.ParkingData();
        palais.setId(12);
        palais.setNom("PALAIS DE JUSTICE");
        palais.setCapacite(228);
        palais.setPlaces(130);
        palais.setGeopoint("46.58595804860371, 0.3512954265806957");
        palais.setInfoParkingsGeoPoint("46.58595804860371, 0.3512954265806957");
        palais.setTauxOccupation(42.9824561403509);
        palais.setDerniereMiseAJour("2025-10-23T11:21:00+02:00");

        PoitiersApiResponse.ParkingData hotelVille = new PoitiersApiResponse.ParkingData();
        hotelVille.setId(2);
        hotelVille.setNom("HOTEL DE VILLE");
        hotelVille.setCapacite(625);
        hotelVille.setPlaces(273);
        hotelVille.setGeopoint("46.5793235337795, 0.3385507838016221");
        hotelVille.setInfoParkingsGeoPoint("46.5793235337795, 0.3385507838016221");
        hotelVille.setTauxOccupation(56.32);
        hotelVille.setDerniereMiseAJour("2025-10-23T11:21:00+02:00");

        PoitiersApiResponse.ParkingData gareToumai = new PoitiersApiResponse.ParkingData();
        gareToumai.setId(9);
        gareToumai.setNom("GARE TOUMAI");
        gareToumai.setCapacite(640);
        gareToumai.setPlaces(231);
        gareToumai.setGeopoint("46.58358353103216, 0.3348348830917244");
        gareToumai.setInfoParkingsGeoPoint("46.58358353103216, 0.3348348830917244");
        gareToumai.setTauxOccupation(63.90625);
        gareToumai.setDerniereMiseAJour("2025-10-23T11:20:59+02:00");

        PoitiersApiResponse.ParkingData gareEffia = new PoitiersApiResponse.ParkingData();
        gareEffia.setId(5);
        gareEffia.setNom("GARE EFFIA");
        gareEffia.setCapacite(480);
        gareEffia.setPlaces(244);
        gareEffia.setGeopoint(null); // No geopoint in real data
        gareEffia.setInfoParkingsGeoPoint(null);
        gareEffia.setTauxOccupation(49.1666666666667);
        gareEffia.setDerniereMiseAJour("2025-10-23T11:20:59+02:00");

        PoitiersApiResponse.ParkingData cordeliers = new PoitiersApiResponse.ParkingData();
        cordeliers.setId(6);
        cordeliers.setNom("CORDELIERS");
        cordeliers.setCapacite(290);
        cordeliers.setPlaces(207);
        cordeliers.setGeopoint(null); // No geopoint in real data
        cordeliers.setInfoParkingsGeoPoint(null);
        cordeliers.setTauxOccupation(28.6206896551724);
        cordeliers.setDerniereMiseAJour("2025-10-23T11:20:59+02:00");

        PoitiersApiResponse.ParkingData blossac = new PoitiersApiResponse.ParkingData();
        blossac.setId(0);
        blossac.setNom("BLOSSAC TISON");
        blossac.setCapacite(665);
        blossac.setPlaces(375);
        blossac.setGeopoint("46.57505317559496, 0.337126307915689");
        blossac.setInfoParkingsGeoPoint("46.57505317559496, 0.337126307915689");
        blossac.setTauxOccupation(43.609022556391);
        blossac.setDerniereMiseAJour("2025-10-23T11:20:58+02:00");

        PoitiersApiResponse.ParkingData arretMinute = new PoitiersApiResponse.ParkingData();
        arretMinute.setId(11);
        arretMinute.setNom("ARRET MINUTE");
        arretMinute.setCapacite(137);
        arretMinute.setPlaces(69);
        arretMinute.setGeopoint("46.583793004495156, 0.3349825350533068");
        arretMinute.setInfoParkingsGeoPoint("46.583793004495156, 0.3349825350533068");
        arretMinute.setTauxOccupation(49.6350364963504);
        arretMinute.setDerniereMiseAJour("2025-10-23T11:20:58+02:00");

        response.setResults(List.of(
                theatre, palais, hotelVille, gareToumai,
                gareEffia, cordeliers, blossac, arretMinute
        ));

        return response;
    }

    private PoitiersApiResponse createMockApiResponse() {
        PoitiersApiResponse response = new PoitiersApiResponse();
        response.setTotal(2);

        PoitiersApiResponse.ParkingData parking1 = new PoitiersApiResponse.ParkingData();
        parking1.setId(1);
        parking1.setNom("Parking Centre");
        parking1.setCapacite(100);
        parking1.setPlaces(50);
        parking1.setGeopoint("46.5802, 0.3404");

        PoitiersApiResponse.ParkingData parking2 = new PoitiersApiResponse.ParkingData();
        parking2.setId(2);
        parking2.setNom("Parking Gare");
        parking2.setCapacite(200);
        parking2.setPlaces(0);
        parking2.setGeopoint("46.5835, 0.3442");

        response.setResults(List.of(parking1, parking2));
        return response;
    }
}

