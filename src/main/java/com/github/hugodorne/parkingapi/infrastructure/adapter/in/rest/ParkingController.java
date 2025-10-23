package com.github.hugodorne.parkingapi.infrastructure.adapter.in.rest;

import com.github.hugodorne.parkingapi.domain.port.in.GetParkingsUseCase;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for parking operations
 */
@RestController
@RequestMapping("/api/parkings")
@RequiredArgsConstructor
@Validated
public class ParkingController {

    private final GetParkingsUseCase getParkingsUseCase;

    /**
     * Get all parkings
     */
    @GetMapping
    public ResponseEntity<List<ParkingResponse>> getAllParkings() {
        List<ParkingResponse> parkings = getParkingsUseCase.getAllParkings()
                .stream()
                .map(ParkingResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(parkings);
    }

    /**
     * Get parkings nearby a specific location
     *
     * @param latitude  User latitude
     * @param longitude User longitude
     * @param radius    Search radius in kilometers (default: 5km)
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<ParkingResponse>> getParkingsNearby(
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
            @RequestParam(defaultValue = "5.0") @Positive double radius
    ) {
        List<ParkingResponse> parkings = getParkingsUseCase
                .getParkingsNearby(latitude, longitude, radius)
                .stream()
                .map(ParkingResponse::fromDomain)
                .toList();

        return ResponseEntity.ok(parkings);
    }
}

