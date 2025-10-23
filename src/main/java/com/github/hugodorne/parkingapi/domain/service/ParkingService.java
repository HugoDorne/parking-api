package com.github.hugodorne.parkingapi.domain.service;

import com.github.hugodorne.parkingapi.domain.model.Parking;
import com.github.hugodorne.parkingapi.domain.port.in.GetParkingsUseCase;
import com.github.hugodorne.parkingapi.domain.port.out.ParkingDataPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Domain service implementing the business logic for parking operations
 */
@Service
@RequiredArgsConstructor
public class ParkingService implements GetParkingsUseCase {

    private final ParkingDataPort parkingDataPort;

    @Override
    public List<Parking> getAllParkings() {
        return parkingDataPort.fetchParkings();
    }

    @Override
    public List<Parking> getParkingsNearby(double latitude, double longitude, double radiusKm) {
        List<Parking> allParkings = parkingDataPort.fetchParkings();

        return allParkings.stream()
                .filter(parking -> parking.getLatitude() != null && parking.getLongitude() != null)
                .map(parking -> {
                    double distance = calculateDistance(
                            latitude, longitude,
                            parking.getLatitude(), parking.getLongitude()
                    );
                    // Add distance to parking using toBuilder
                    return parking.toBuilder()
                            .distanceKm(Math.round(distance * 100.0) / 100.0)  // Round to 2 decimal places
                            .build();
                })
                .filter(parking -> parking.getDistanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(Parking::getDistanceKm))
                .toList();
    }

    /**
     * Calculate distance between two points using Haversine formula
     *
     * @return distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}

