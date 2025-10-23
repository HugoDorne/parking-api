package com.github.hugodorne.parkingapi.infrastructure.adapter.in.rest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RestExceptionHandler
 */
class RestExceptionHandlerTest {

    private RestExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new RestExceptionHandler();
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "latitude", "must be less than or equal to 90");
        FieldError fieldError2 = new FieldError("object", "longitude", "must be less than or equal to 180");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid input parameters");
        assertThat(response.getBody().getErrors()).hasSize(2);
        assertThat(response.getBody().getErrors().get("latitude")).isEqualTo("must be less than or equal to 90");
        assertThat(response.getBody().getErrors().get("longitude")).isEqualTo("must be less than or equal to 180");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void shouldHandleConstraintViolationException() {
        // Given
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);

        when(violation1.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation1.getPropertyPath().toString()).thenReturn("getParkingsNearby.latitude");
        when(violation1.getMessage()).thenReturn("must be less than or equal to 90");

        when(violation2.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation2.getPropertyPath().toString()).thenReturn("getParkingsNearby.radius");
        when(violation2.getMessage()).thenReturn("must be greater than 0");

        ConstraintViolationException exception = new ConstraintViolationException(
                "Validation failed",
                Set.of(violation1, violation2)
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid input parameters");
        assertThat(response.getBody().getErrors()).hasSize(2);
        assertThat(response.getBody().getErrors().get("latitude")).isEqualTo("must be less than or equal to 90");
        assertThat(response.getBody().getErrors().get("radius")).isEqualTo("must be greater than 0");
    }

    @Test
    void shouldHandleTypeMismatchExceptionWithNullRequiredType() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("radius");
        when(exception.getRequiredType()).thenReturn(null);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatchException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors().get("radius")).contains("unknown");
    }

    @Test
    void shouldHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    void shouldExtractSimpleFieldNameFromPropertyPath() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("latitude");
        when(violation.getMessage()).thenReturn("must be valid");

        ConstraintViolationException exception = new ConstraintViolationException(
                "Validation failed",
                Set.of(violation)
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(exception);

        // Then
        assertThat(response.getBody().getErrors()).containsKey("latitude");
    }
}
