package com.github.hugodorne.parkingapi.infrastructure.adapter.out.poitiers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Response model for Poitiers API
 */
@Data
public class PoitiersApiResponse {
    private Integer total;
    private List<ParkingData> results;

    @Data
    public static class ParkingData {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("Nom")
        private String nom;

        @JsonProperty("Capacite")
        private Integer capacite;

        @JsonProperty("Places")
        private Integer places;

        @JsonProperty("taux_doccupation")
        private Double tauxOccupation;

        @JsonProperty("_geopoint")
        private String geopoint;

        @JsonProperty("infos_parkingsgeo_point")
        private String infoParkingsGeoPoint;

        @JsonProperty("Dernière_mise_à_jour_Base")
        private String derniereMiseAJour;
    }
}

