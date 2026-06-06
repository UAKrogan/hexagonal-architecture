package com.example.hexagonal.adapter.in.web.context;

import com.example.hexagonal.infrastructure.context.RequestContext;
import com.example.hexagonal.infrastructure.context.RequestContextHeaders;
import com.example.hexagonal.infrastructure.http.propagation.HeaderPropagationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RequestContextWebFilterTest {

    private final HeaderPropagationProperties properties = new HeaderPropagationProperties(
        List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION, "x-allowed")
    );
    private final RequestContextWebFilter requestContextWebFilter = new RequestContextWebFilter(properties);

    @Test
    void shouldCreateRequestContextFromAllowedHeaders() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/api/content")
                .header(RequestContextHeaders.CORRELATION_ID, "correlation-id")
                .header(RequestContextHeaders.API_VERSION, "1")
                .header("x-allowed", "allowed-value")
                .header("x-not-allowed", "not-allowed-value")
        );

        StepVerifier.create(requestContextWebFilter.filter(
                exchange,
                currentExchange -> Mono.deferContextual(contextView -> Mono.just(contextView.get(RequestContext.class)))
                    .cast(RequestContext.class)
                    .doOnNext(requestContext -> assertThat(requestContext.headers())
                        .containsEntry(RequestContextHeaders.CORRELATION_ID, "correlation-id")
                        .containsEntry(RequestContextHeaders.API_VERSION, "1")
                        .containsEntry("x-allowed", "allowed-value")
                        .doesNotContainKey("x-not-allowed"))
                    .then()
            ))
            .verifyComplete();
    }

    @Test
    void shouldUseFallbackValuesWhenRequiredHeadersAreMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/content"));

        StepVerifier.create(requestContextWebFilter.filter(
                exchange,
                currentExchange -> Mono.deferContextual(contextView -> Mono.just(contextView.get(RequestContext.class)))
                    .cast(RequestContext.class)
                    .doOnNext(requestContext -> {
                        assertThat(requestContext.correlationId()).isNotBlank();
                        assertThat(requestContext.apiVersion()).isEqualTo("unknown");
                        assertThat(requestContext.headers())
                            .containsEntry(RequestContextHeaders.API_VERSION, "unknown")
                            .containsKey(RequestContextHeaders.CORRELATION_ID);
                    })
                    .then()
            ))
            .verifyComplete();
    }
}
