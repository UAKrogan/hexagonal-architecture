package com.example.hexagonal.infrastructure.http.propagation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderPropagationPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldNormalizeConfiguredHeaders() {
        HeaderPropagationProperties properties = new HeaderPropagationProperties(
            List.of("X-Correlation-Id", "X-Api-Version", "X-Test")
        );

        assertThat(properties.normalizedHeaders())
            .containsExactlyInAnyOrder("x-correlation-id", "x-api-version", "x-test");
        assertThat(properties.shouldPropagate("X-Test")).isTrue();
        assertThat(properties.shouldPropagate("x-not-allowed")).isFalse();
    }

    @Test
    void shouldPassValidationWhenRequiredHeadersAreConfigured() {
        HeaderPropagationProperties properties = new HeaderPropagationProperties(
            List.of("x-correlation-id", "x-api-version")
        );

        assertThat(validator.validate(properties)).isEmpty();
    }

    @Test
    void shouldFailValidationWhenRequiredHeadersAreMissing() {
        HeaderPropagationProperties properties = new HeaderPropagationProperties(List.of("x-correlation-id"));

        assertThat(validator.validate(properties))
            .anyMatch(violation -> violation.getMessage().contains("x-correlation-id and x-api-version"));
    }

    @Test
    void shouldFailValidationWhenHeaderListIsMissing() {
        HeaderPropagationProperties properties = new HeaderPropagationProperties(null);

        assertThat(validator.validate(properties))
            .anyMatch(violation -> violation.getPropertyPath().toString().equals("headers"));
    }
}
