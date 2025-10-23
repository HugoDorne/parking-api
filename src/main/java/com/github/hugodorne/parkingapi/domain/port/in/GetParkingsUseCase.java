package com.github.hugodorne.parkingapi.domain.port.in;

import com.github.hugodorne.parkingapi.domain.model.Parking;

import java.util.List;

/**
 * Input port for retrieving parkings
 */
public interface GetParkingsUseCase {

    /**
     * Get all available parkings
     */
    List<Parking> getAllParkings();

    /**
     * Get parkings near a specific location
     *
     * @param latitude  User latitude
     * @param longitude User longitude
     * @param radiusKm  Search radius in kilometers
     */
    List<Parking> getParkingsNearby(double latitude, double longitude, double radiusKm);
}

