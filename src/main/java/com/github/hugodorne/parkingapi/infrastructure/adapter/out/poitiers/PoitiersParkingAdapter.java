package com.github.hugodorne.parkingapi.infrastructure.adapter.out.poitiers;

import com.github.hugodorne.parkingapi.domain.model.Parking;
import com.github.hugodorne.parkingapi.domain.model.ParkingStatus;
import com.github.hugodorne.parkingapi.domain.port.out.ParkingDataPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Adapter for Poitiers parking data source
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PoitiersParkingAdapter implements ParkingDataPort {

    private final RestTemplate restTemplate;
    private final PoitiersParkingProperties properties;

    @Override
    @Cacheable(value = "parkings", unless = "#result == null || #result.isEmpty()")
    public List<Parking> fetchParkings() {
        log.info("Fetching parkings from Poitiers data source: {}", properties.getUrl());

        try {
            PoitiersApiResponse response = restTemplate.getForObject(
                    properties.getUrl(),
                    PoitiersApiResponse.class
            );

            if (response == null || response.getResults() == null) {
                log.warn("No data received from Poitiers API");
                return List.of();
            }

            return response.getResults().stream()
                    .map(this::mapToParking)
                    .toList();

        } catch (Exception e) {
            log.error("Error fetching parkings from Poitiers API", e);
            return List.of();
        }
    }

    private Parking mapToParking(PoitiersApiResponse.ParkingData data) {
        // Extract coordinates from geopoint string (format: "latitude, longitude")
        Double latitude = null;
        Double longitude = null;

        String geopoint = data.getGeopoint();
        if (geopoint == null) {
            geopoint = data.getInfoParkingsGeoPoint();
        }

        if (geopoint != null && !geopoint.isEmpty()) {
            try {
                String[] coords = geopoint.split(",");
                if (coords.length == 2) {
                    latitude = Double.parseDouble(coords[0].trim());
                    longitude = Double.parseDouble(coords[1].trim());
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse geopoint: {}", geopoint);
            }
        }

        // Extract capacities
        Integer totalSpaces = data.getCapacite();
        Integer availableSpaces = data.getPlaces();

        // Determine status based on available spaces
        ParkingStatus status = determineStatus(totalSpaces, availableSpaces);

        // Use ID as string identifier
        String parkingId = data.getId() != null ? data.getId().toString() : data.getNom();

        return Parking.builder()
                .id(parkingId)
                .name(data.getNom())
                .address(null)  // No address field in the API
                .latitude(latitude)
                .longitude(longitude)
                .totalSpaces(totalSpaces)
                .availableSpaces(availableSpaces)
                .status(status)
                .build();
    }

    private ParkingStatus determineStatus(Integer totalSpaces, Integer availableSpaces) {
        if (availableSpaces == null || totalSpaces == null) {
            return ParkingStatus.UNKNOWN;
        }

        if (availableSpaces == 0) {
            return ParkingStatus.FULL;
        }

        // If there are available spaces, consider it open
        return ParkingStatus.OPEN;
    }
}

