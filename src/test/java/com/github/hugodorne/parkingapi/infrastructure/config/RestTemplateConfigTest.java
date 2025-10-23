package com.github.hugodorne.parkingapi.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RestTemplateConfig
 */
class RestTemplateConfigTest {

    @Test
    void shouldCreateRestTemplateWithTimeouts() {
        // Given
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate mockRestTemplate = new RestTemplate();

        when(builder.connectTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.readTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.build()).thenReturn(mockRestTemplate);

        // When
        RestTemplate restTemplate = config.restTemplate(builder);

        // Then
        assertThat(restTemplate).isNotNull();
        verify(builder).connectTimeout(Duration.ofSeconds(10));
        verify(builder).readTimeout(Duration.ofSeconds(10));
        verify(builder).build();
    }
}

