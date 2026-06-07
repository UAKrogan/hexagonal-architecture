package com.example.hexagonal.infrastructure.observability.context;

import com.example.hexagonal.test.tag.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class RequestContextFactoryTest {

    private final RequestContextFactory requestContextFactory = new RequestContextFactory();

    @Test
    void shouldCreateRequestContextFromAllowedHeaders() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(
                RequestContextHeaders.CORRELATION_ID, "correlation-id",
                RequestContextHeaders.API_VERSION, "1",
                "x-allowed", "allowed-value",
                "x-not-allowed", "not-allowed-value"
            ),
            List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION, "x-allowed"),
            "fallback-correlation-id"
        );

        assertThat(requestContext.correlationId()).isEqualTo("correlation-id");
        assertThat(requestContext.apiVersion()).isEqualTo("1");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, "correlation-id")
            .containsEntry(RequestContextHeaders.API_VERSION, "1")
            .containsEntry("x-allowed", "allowed-value")
            .doesNotContainKey("x-not-allowed");
    }

    @Test
    void shouldUseFallbackValuesWhenRequiredHeadersAreMissing() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(),
            List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION),
            "fallback-correlation-id"
        );

        assertThat(requestContext.correlationId()).isEqualTo("fallback-correlation-id");
        assertThat(requestContext.apiVersion()).isEqualTo("unknown");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, "fallback-correlation-id")
            .containsEntry(RequestContextHeaders.API_VERSION, "unknown");
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderAndFallbackAreMissing() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(),
            List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION),
            null
        );

        assertThat(requestContext.correlationId()).isNotBlank();
        assertThat(requestContext.apiVersion()).isEqualTo("unknown");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, requestContext.correlationId())
            .containsEntry(RequestContextHeaders.API_VERSION, "unknown");
    }

    @Test
    void shouldNormalizeHeaderNamesForInboundAdapters() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(
                "X-Correlation-ID", "correlation-id",
                "X-API-Version", "1",
                "X-Allowed", "allowed-value"
            ),
            List.of("x-correlation-id", "x-api-version", "x-allowed"),
            "fallback-correlation-id"
        );

        assertThat(requestContext.correlationId()).isEqualTo("correlation-id");
        assertThat(requestContext.apiVersion()).isEqualTo("1");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, "correlation-id")
            .containsEntry(RequestContextHeaders.API_VERSION, "1")
            .containsEntry("x-allowed", "allowed-value");
    }

    @Test
    void shouldUseResolvedValuesForBlankRequiredHeadersAndKeepBlankAllowedHeaderValue() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(
                RequestContextHeaders.CORRELATION_ID, " ",
                RequestContextHeaders.API_VERSION, " ",
                "x-allowed", " "
            ),
            List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION, "x-allowed"),
            "fallback-correlation-id"
        );

        assertThat(requestContext.correlationId()).isEqualTo("fallback-correlation-id");
        assertThat(requestContext.apiVersion()).isEqualTo("unknown");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, "fallback-correlation-id")
            .containsEntry(RequestContextHeaders.API_VERSION, "unknown")
            .containsEntry("x-allowed", " ");
    }
}
