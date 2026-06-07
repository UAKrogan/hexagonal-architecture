package com.example.hexagonal.infrastructure.observability.context;

import com.example.hexagonal.test.tag.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class RequestContextFactoryTest {

    private static final String CORRELATION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final UUID FALLBACK_CORRELATION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    private final RequestContextFactory requestContextFactory = new RequestContextFactory();

    @Test
    void shouldCreateRequestContextFromAllowedHeaders() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(
                RequestContextHeaders.CORRELATION_ID, CORRELATION_ID,
                RequestContextHeaders.API_VERSION, "1",
                "x-allowed", "allowed-value",
                "x-not-allowed", "not-allowed-value"
            ),
            List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION, "x-allowed"),
            FALLBACK_CORRELATION_ID
        );

        assertThat(requestContext.correlationId()).isEqualTo(UUID.fromString(CORRELATION_ID));
        assertThat(requestContext.apiVersion()).isEqualTo("1");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
            .containsEntry(RequestContextHeaders.API_VERSION, "1")
            .containsEntry("x-allowed", "allowed-value")
            .doesNotContainKey("x-not-allowed");
    }

    @Test
    void shouldUseFallbackValuesWhenRequiredHeadersAreMissing() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(),
            List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION),
            FALLBACK_CORRELATION_ID
        );

        assertThat(requestContext.correlationId()).isEqualTo(FALLBACK_CORRELATION_ID);
        assertThat(requestContext.apiVersion()).isEqualTo("unknown");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, FALLBACK_CORRELATION_ID.toString())
            .containsEntry(RequestContextHeaders.API_VERSION, "unknown");
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderAndFallbackAreMissing() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(),
            List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION),
            null
        );

        assertThat(requestContext.correlationId()).isNotNull();
        assertThat(requestContext.apiVersion()).isEqualTo("unknown");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, requestContext.correlationId().toString())
            .containsEntry(RequestContextHeaders.API_VERSION, "unknown");
    }

    @Test
    void shouldNormalizeHeaderNamesForInboundAdapters() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(
                "X-Correlation-ID", CORRELATION_ID,
                "X-API-Version", "1",
                "X-Allowed", "allowed-value"
            ),
            List.of("x-correlation-id", "x-api-version", "x-allowed"),
            FALLBACK_CORRELATION_ID
        );

        assertThat(requestContext.correlationId()).isEqualTo(UUID.fromString(CORRELATION_ID));
        assertThat(requestContext.apiVersion()).isEqualTo("1");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
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
            FALLBACK_CORRELATION_ID
        );

        assertThat(requestContext.correlationId()).isEqualTo(FALLBACK_CORRELATION_ID);
        assertThat(requestContext.apiVersion()).isEqualTo("unknown");
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, FALLBACK_CORRELATION_ID.toString())
            .containsEntry(RequestContextHeaders.API_VERSION, "unknown")
            .containsEntry("x-allowed", " ");
    }

    @Test
    void shouldUseFallbackCorrelationIdInternallyAndPreserveInvalidInboundHeaderForPropagation() {
        RequestContext requestContext = requestContextFactory.create(
            Map.of(RequestContextHeaders.CORRELATION_ID, "not-a-uuid"),
            List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION),
            FALLBACK_CORRELATION_ID
        );

        assertThat(requestContext.correlationId()).isEqualTo(FALLBACK_CORRELATION_ID);
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, "not-a-uuid")
            .containsEntry(RequestContextHeaders.API_VERSION, "unknown");
    }
}
