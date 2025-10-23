package com.github.hugodorne.parkingapi.infrastructure.adapter.out.poitiers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PoitiersParkingProperties
 */
class PoitiersParkingPropertiesTest {

    @Test
    void shouldHaveDefaultUrl() {
        // Given
        PoitiersParkingProperties properties = new PoitiersParkingProperties();

        // When
        String url = properties.getUrl();

        // Then
        assertThat(url).isEqualTo("https://data.grandpoitiers.fr/data-fair/api/v1/datasets/mobilites-stationnement-des-parkings-en-temps-reel/lines");
    }

    @Test
    void shouldAllowSettingCustomUrl() {
        // Given
        PoitiersParkingProperties properties = new PoitiersParkingProperties();
        String customUrl = "https://custom-api.example.com/parkings";

        // When
        properties.setUrl(customUrl);

        // Then
        assertThat(properties.getUrl()).isEqualTo(customUrl);
    }
}

