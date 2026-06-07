package com.example.hexagonal.infrastructure.observability.logging;

import com.example.hexagonal.test.tag.UnitTest;
import com.example.hexagonal.infrastructure.observability.context.RequestContext;
import com.example.hexagonal.infrastructure.observability.context.RequestContextHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class RequestContextMdcThreadLocalAccessorTest {

    private final RequestContextMdcThreadLocalAccessor accessor = new RequestContextMdcThreadLocalAccessor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldSetAndReadMdcContextMap() {
        Map<String, String> mdcContext = Map.of(
            RequestContextHeaders.CORRELATION_ID, "correlation-id",
            RequestContextHeaders.API_VERSION, "1"
        );

        accessor.setValue(mdcContext);

        assertThat(MDC.get(RequestContextHeaders.CORRELATION_ID)).isEqualTo("correlation-id");
        assertThat(MDC.get(RequestContextHeaders.API_VERSION)).isEqualTo("1");
        assertThat(accessor.getValue()).containsAllEntriesOf(mdcContext);
    }

    @Test
    void shouldClearMdcWhenValueIsEmptyOrMissing() {
        MDC.put(RequestContextHeaders.CORRELATION_ID, "correlation-id");

        accessor.setValue(Map.of());

        assertThat(MDC.getCopyOfContextMap()).isNull();

        MDC.put(RequestContextHeaders.CORRELATION_ID, "correlation-id");

        accessor.setValue();

        assertThat(MDC.getCopyOfContextMap()).isNull();
    }

    @Test
    void shouldCreateMdcContextFromRequestContext() {
        UUID correlationId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        RequestContext requestContext = new RequestContext(
            correlationId,
            "1",
            Map.of(RequestContextHeaders.CORRELATION_ID, correlationId.toString())
        );

        assertThat(RequestContextMdcThreadLocalAccessor.toMdcContext(requestContext))
            .containsEntry(RequestContextHeaders.CORRELATION_ID, correlationId.toString())
            .containsEntry(RequestContextHeaders.API_VERSION, "1");
    }
}
