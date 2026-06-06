package com.example.hexagonal.infrastructure.logging;

import com.example.hexagonal.infrastructure.context.RequestContext;
import com.example.hexagonal.infrastructure.context.RequestContextHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
        RequestContext requestContext = new RequestContext(
            "correlation-id",
            "1",
            Map.of(RequestContextHeaders.CORRELATION_ID, "correlation-id")
        );

        assertThat(RequestContextMdcThreadLocalAccessor.toMdcContext(requestContext))
            .containsEntry(RequestContextHeaders.CORRELATION_ID, "correlation-id")
            .containsEntry(RequestContextHeaders.API_VERSION, "1");
    }
}
