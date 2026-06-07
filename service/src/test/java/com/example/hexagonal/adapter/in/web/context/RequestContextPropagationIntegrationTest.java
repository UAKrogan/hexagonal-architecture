package com.example.hexagonal.adapter.in.web.context;

import com.example.hexagonal.test.tag.FunctionalTest;
import com.example.hexagonal.infrastructure.observability.context.RequestContext;
import com.example.hexagonal.infrastructure.observability.context.RequestContextHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "application.http.propagation.headers[0]=x-correlation-id",
        "application.http.propagation.headers[1]=x-api-version",
        "application.http.propagation.headers[2]=x-test-allowed"
    }
)
@AutoConfigureWebTestClient
@FunctionalTest
class RequestContextPropagationIntegrationTest {

    private static final String CORRELATION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String API_VERSION = "1";
    private static final String ALLOWED_HEADER = "x-test-allowed";
    private static final String NOT_ALLOWED_HEADER = "x-not-propagated";

    private static final LinkedBlockingQueue<DownstreamRequest> DOWNSTREAM_REQUESTS = new LinkedBlockingQueue<>();

    private static final DisposableServer DOWNSTREAM_SERVER = HttpServer.create()
        .port(0)
        .handle((request, response) -> {
            DOWNSTREAM_REQUESTS.add(DownstreamRequest.from(request));

            return response
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .sendString(Mono.just("""
                    {
                      "userId": 10,
                      "id": 1,
                      "title": "mock-title",
                      "body": "mock-body"
                    }
                    """));
        })
        .bindNow();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ContextCaptureProbe contextCaptureProbe;

    @DynamicPropertySource
    static void downstreamProperties(DynamicPropertyRegistry registry) {
        String baseUrl = "http://localhost:" + DOWNSTREAM_SERVER.port();
        registry.add("application.http.clients.json-placeholder.base-url", () -> baseUrl);
        registry.add("application.http.clients.dummy-json.base-url", () -> baseUrl);
    }

    @BeforeEach
    void setUp() {
        DOWNSTREAM_REQUESTS.clear();
        contextCaptureProbe.reset();
    }

    @AfterAll
    static void tearDown() {
        DOWNSTREAM_SERVER.disposeNow();
    }

    @Test
    void shouldCreateRequestContextPropagateMdcAndPropagateOnlyAllowedOutboundHeaders() throws Exception {
        webTestClient.post()
            .uri("/api/content")
            .contentType(MediaType.APPLICATION_JSON)
            .header(RequestContextHeaders.API_VERSION, API_VERSION)
            .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
            .header(ALLOWED_HEADER, "allowed-value")
            .header(NOT_ALLOWED_HEADER, "not-allowed-value")
            .bodyValue("""
                {
                  "contentId": 1,
                  "provider": "JSONPLACEHOLDER"
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.source").isEqualTo("JSONPLACEHOLDER");

        DownstreamRequest downstreamRequest = DOWNSTREAM_REQUESTS.poll(5, TimeUnit.SECONDS);

        assertThat(downstreamRequest).isNotNull();
        assertThat(downstreamRequest.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
            .containsEntry(RequestContextHeaders.API_VERSION, API_VERSION)
            .containsEntry(ALLOWED_HEADER, "allowed-value")
            .doesNotContainKey(NOT_ALLOWED_HEADER);

        RequestContext requestContext = contextCaptureProbe.requestContext();

        assertThat(requestContext).isNotNull();
        assertThat(requestContext.correlationId()).isEqualTo(CORRELATION_ID);
        assertThat(requestContext.apiVersion()).isEqualTo(API_VERSION);
        assertThat(requestContext.headers())
            .containsEntry(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
            .containsEntry(RequestContextHeaders.API_VERSION, API_VERSION)
            .containsEntry(ALLOWED_HEADER, "allowed-value")
            .doesNotContainKey(NOT_ALLOWED_HEADER);

        assertThat(contextCaptureProbe.mapMdcCorrelationId()).isEqualTo(CORRELATION_ID);
        assertThat(contextCaptureProbe.mapMdcApiVersion()).isEqualTo(API_VERSION);
        assertThat(contextCaptureProbe.flatMapMdcCorrelationId()).isEqualTo(CORRELATION_ID);
        assertThat(contextCaptureProbe.flatMapMdcApiVersion()).isEqualTo(API_VERSION);
    }

    @TestConfiguration
    static class ContextCaptureConfiguration {

        @Bean
        ContextCaptureProbe contextCaptureProbe() {
            return new ContextCaptureProbe();
        }

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE + 1)
        WebFilter contextCaptureWebFilter(ContextCaptureProbe contextCaptureProbe) {
            return (exchange, chain) -> Mono.deferContextual(contextView -> {
                contextCaptureProbe.requestContext.set(contextView.get(RequestContext.class));

                return Flux.just("probe")
                    .publishOn(Schedulers.boundedElastic())
                    .map(value -> {
                        contextCaptureProbe.mapMdcCorrelationId.set(MDC.get(RequestContextHeaders.CORRELATION_ID));
                        contextCaptureProbe.mapMdcApiVersion.set(MDC.get(RequestContextHeaders.API_VERSION));
                        return value;
                    })
                    .flatMap(value -> Mono.fromCallable(() -> {
                            contextCaptureProbe.flatMapMdcCorrelationId.set(MDC.get(RequestContextHeaders.CORRELATION_ID));
                            contextCaptureProbe.flatMapMdcApiVersion.set(MDC.get(RequestContextHeaders.API_VERSION));
                            return value;
                        })
                        .subscribeOn(Schedulers.parallel()))
                    .timeout(Duration.ofSeconds(5))
                    .then(chain.filter(exchange));
            });
        }
    }

    static class ContextCaptureProbe {

        private final AtomicReference<RequestContext> requestContext = new AtomicReference<>();
        private final AtomicReference<String> mapMdcCorrelationId = new AtomicReference<>();
        private final AtomicReference<String> mapMdcApiVersion = new AtomicReference<>();
        private final AtomicReference<String> flatMapMdcCorrelationId = new AtomicReference<>();
        private final AtomicReference<String> flatMapMdcApiVersion = new AtomicReference<>();

        void reset() {
            requestContext.set(null);
            mapMdcCorrelationId.set(null);
            mapMdcApiVersion.set(null);
            flatMapMdcCorrelationId.set(null);
            flatMapMdcApiVersion.set(null);
        }

        RequestContext requestContext() {
            return requestContext.get();
        }

        String mapMdcCorrelationId() {
            return mapMdcCorrelationId.get();
        }

        String mapMdcApiVersion() {
            return mapMdcApiVersion.get();
        }

        String flatMapMdcCorrelationId() {
            return flatMapMdcCorrelationId.get();
        }

        String flatMapMdcApiVersion() {
            return flatMapMdcApiVersion.get();
        }
    }

    record DownstreamRequest(Map<String, String> headers) {

        static DownstreamRequest from(HttpServerRequest request) {
            Map<String, String> headers = new HashMap<>();
            request.requestHeaders()
                .forEach(header -> headers.put(header.getKey().toLowerCase(Locale.ROOT), header.getValue()));

            return new DownstreamRequest(Map.copyOf(headers));
        }
    }
}
