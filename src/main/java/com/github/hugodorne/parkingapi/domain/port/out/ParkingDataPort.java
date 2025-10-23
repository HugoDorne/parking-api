package com.github.hugodorne.parkingapi.domain.port.out;

import com.github.hugodorne.parkingapi.domain.model.Parking;

import java.util.List;

/**
 * Output port for retrieving parking data from external sources
 */
public interface ParkingDataPort {

    /**
     * Fetch all parkings from the data source
     */
    List<Parking> fetchParkings();
}

