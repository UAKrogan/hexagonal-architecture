package com.example.hexagonal.adapter.in.web;

import com.example.hexagonal.test.tag.FunctionalTest;
import com.example.hexagonal.infrastructure.observability.context.RequestContextHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@FunctionalTest
class ContentApiIntegrationTest {

    private static final String CORRELATION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String SUCCESS_BODY = """
        {
          "userId": 10,
          "id": 1,
          "title": "mock-title",
          "body": "mock-body"
        }
        """;

    private static final DisposableServer DOWNSTREAM_SERVER = HttpServer.create()
        .port(0)
        .handle((request, response) -> {
            if (request.uri().endsWith("/404")) {
                return response.status(404).send();
            }
            if (request.uri().endsWith("/500")) {
                return response.status(500).send();
            }

            return response
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .sendString(Mono.just(SUCCESS_BODY));
        })
        .bindNow();

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void downstreamProperties(DynamicPropertyRegistry registry) {
        String baseUrl = "http://localhost:" + DOWNSTREAM_SERVER.port();
        registry.add("application.http.clients.json-placeholder.base-url", () -> baseUrl);
        registry.add("application.http.clients.dummy-json.base-url", () -> baseUrl);
    }

    @AfterAll
    static void tearDown() {
        DOWNSTREAM_SERVER.disposeNow();
    }

    @Test
    void shouldRouteVersionOneRequestAndReturnContent() {
        webTestClient.post()
            .uri("/api/content")
            .contentType(MediaType.APPLICATION_JSON)
            .header(RequestContextHeaders.API_VERSION, "1")
            .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
            .bodyValue(validBody(1L, "JSONPLACEHOLDER"))
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.title").isEqualTo("mock-title")
            .jsonPath("$.content").isEqualTo("mock-body")
            .jsonPath("$.authorId").isEqualTo(10)
            .jsonPath("$.source").isEqualTo("JSONPLACEHOLDER");
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyFailsValidation() {
        expectProblemResponse(
            webTestClient.post()
                .uri("/api/content")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestContextHeaders.API_VERSION, "1")
                .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
                .bodyValue("""
                    {
                      "provider": "JSONPLACEHOLDER"
                    }
                    """),
            400,
            "urn:problem:hexagonal:validation-error"
        )
            .jsonPath("$.validationErrors[0].field").value(field -> assertThat(field.toString()).contains("contentId"));
    }

    @Test
    void shouldReturnBadRequestWhenCorrelationIdHeaderIsMissing() {
        expectProblemResponse(
            webTestClient.post()
                .uri("/api/content")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestContextHeaders.API_VERSION, "1")
                .bodyValue(validBody(1L, "JSONPLACEHOLDER")),
            400,
            "urn:problem:hexagonal:missing-header"
        );
    }

    @Test
    void shouldReturnBadRequestWhenApiVersionHeaderIsMissing() {
        expectProblemResponse(
            webTestClient.post()
                .uri("/api/content")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
                .bodyValue(validBody(1L, "JSONPLACEHOLDER")),
            400,
            "urn:problem:hexagonal:missing-header"
        );
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsMalformed() {
        expectProblemResponse(
            webTestClient.post()
                .uri("/api/content")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestContextHeaders.API_VERSION, "1")
                .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
                .bodyValue("""
                    {
                      "contentId": 1,
                      "provider": "JSONPLACEHOLDER"
                    """),
            400,
            "urn:problem:hexagonal:invalid-json"
        );
    }

    @Test
    void shouldReturnUnsupportedMediaTypeProblem() {
        expectProblemResponse(
            webTestClient.post()
                .uri("/api/content")
                .contentType(MediaType.TEXT_PLAIN)
                .header(RequestContextHeaders.API_VERSION, "1")
                .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
                .bodyValue("contentId=1&provider=JSONPLACEHOLDER"),
            415,
            "urn:problem:hexagonal:unsupported-media-type"
        );
    }

    @Test
    void shouldReturnMethodNotAllowedProblem() {
        expectProblemResponse(
            webTestClient.put()
                .uri("/api/content")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestContextHeaders.API_VERSION, "1")
                .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
                .bodyValue(validBody(1L, "JSONPLACEHOLDER")),
            405,
            "urn:problem:hexagonal:method-not-allowed"
        );
    }

    @Test
    void shouldReturnBadRequestWhenApiVersionDoesNotMatch() {
        expectProblemResponse(
            webTestClient.post()
                .uri("/api/content")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestContextHeaders.API_VERSION, "2")
                .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
                .bodyValue(validBody(1L, "JSONPLACEHOLDER")),
            400,
            "urn:problem:hexagonal:invalid-api-version"
        );
    }

    @Test
    void shouldReturnRouteNotFoundProblem() {
        expectProblemResponse(
            webTestClient.post()
                .uri("/api/content/not-found")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestContextHeaders.API_VERSION, "1")
                .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
                .bodyValue(validBody(1L, "JSONPLACEHOLDER")),
            404,
            "urn:problem:hexagonal:route-not-found"
        );
    }

    @Test
    void shouldReturnContentNotFoundProblemWhenProviderReturnsNotFound() {
        expectProblemResponse(
            webTestClient.post()
                .uri("/api/content")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestContextHeaders.API_VERSION, "1")
                .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
                .bodyValue(validBody(404L, "JSONPLACEHOLDER")),
            404,
            "urn:problem:hexagonal:content-not-found"
        );
    }

    @Test
    void shouldReturnBadGatewayProblemWhenProviderReturnsServerError() {
        expectProblemResponse(
            webTestClient.post()
                .uri("/api/content")
                .contentType(MediaType.APPLICATION_JSON)
                .header(RequestContextHeaders.API_VERSION, "1")
                .header(RequestContextHeaders.CORRELATION_ID, CORRELATION_ID)
                .bodyValue(validBody(500L, "JSONPLACEHOLDER")),
            502,
            "urn:problem:hexagonal:provider-unavailable"
        );
    }

    private WebTestClient.BodyContentSpec expectProblemResponse(WebTestClient.RequestHeadersSpec<?> request,
                                                               int status,
                                                               String problemType) {
        return request.exchange()
            .expectStatus().isEqualTo(status)
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath(problemPath(status) + ".type").isEqualTo(problemType);
    }

    private String problemPath(int status) {
        return status == 400 ? "$.problem" : "$";
    }

    private String validBody(Long contentId, String provider) {
        return """
            {
              "contentId": %d,
              "provider": "%s"
            }
            """.formatted(contentId, provider);
    }
}
