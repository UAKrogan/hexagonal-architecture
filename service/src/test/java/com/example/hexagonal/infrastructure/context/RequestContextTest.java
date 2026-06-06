package com.example.hexagonal.infrastructure.context;

import com.example.hexagonal.test.tag.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@UnitTest
class RequestContextTest {

    @Test
    void shouldCreateImmutableHeaderSnapshot() {
        Map<String, String> headers = new HashMap<>();
        headers.put(RequestContextHeaders.CORRELATION_ID, "correlation-id");

        RequestContext requestContext = new RequestContext("correlation-id", "1", headers);

        headers.put("x-new", "new-value");

        assertThat(requestContext.headers())
            .containsOnly(Map.entry(RequestContextHeaders.CORRELATION_ID, "correlation-id"));
        assertThatThrownBy(() -> requestContext.headers().put("x-other", "other"))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
