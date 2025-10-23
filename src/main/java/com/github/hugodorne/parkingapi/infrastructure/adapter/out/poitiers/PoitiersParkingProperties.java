package com.github.hugodorne.parkingapi.infrastructure.adapter.out.poitiers;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Poitiers parking data source
 */
@Component
@ConfigurationProperties(prefix = "parking.data-source.poitiers")
@Data
public class PoitiersParkingProperties {
    private String url = "https://data.grandpoitiers.fr/data-fair/api/v1/datasets/mobilites-stationnement-des-parkings-en-temps-reel/lines";
}

