package com.example.hexagonal.adapter.out.jsonplaceholder.client;

import com.example.hexagonal.test.tag.IntegrationTest;
import com.example.hexagonal.application.exception.ProviderUnavailableException;
import com.example.hexagonal.domain.exception.ContentNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class JsonPlaceholderClientTest {

    private DisposableServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @Test
    void shouldCallExpectedPathAndDecodeSuccessfulResponse() {
        AtomicReference<String> requestedPath = new AtomicReference<>();
        server = server(HttpStatus.OK.value(), """
            {
              "userId": 10,
              "id": 1,
              "title": "title",
              "body": "body"
            }
            """, requestedPath);
        JsonPlaceholderClient client = client();

        StepVerifier.create(client.getContent(1L))
            .assertNext(response -> {
                assertThat(response.userId()).isEqualTo(10L);
                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.title()).isEqualTo("title");
                assertThat(response.body()).isEqualTo("body");
            })
            .verifyComplete();

        assertThat(requestedPath.get()).isEqualTo("/posts/1");
    }

    @Test
    void shouldMapNotFoundResponseToContentNotFoundException() {
        server = server(HttpStatus.NOT_FOUND.value(), "{}", new AtomicReference<>());
        JsonPlaceholderClient client = client();

        StepVerifier.create(client.getContent(99L))
            .expectErrorSatisfies(error -> assertThat(error)
                .isInstanceOf(ContentNotFoundException.class)
                .hasMessage("Content not found for id: 99"))
            .verify();
    }

    @Test
    void shouldMapServerErrorToProviderUnavailableException() {
        server = server(HttpStatus.INTERNAL_SERVER_ERROR.value(), "{}", new AtomicReference<>());
        JsonPlaceholderClient client = client();

        StepVerifier.create(client.getContent(1L))
            .expectErrorSatisfies(error -> assertThat(error)
                .isInstanceOf(ProviderUnavailableException.class)
                .hasMessage("JSONPlaceholder provider is unavailable"))
            .verify();
    }

    private DisposableServer server(int status, String body, AtomicReference<String> requestedPath) {
        return HttpServer.create()
            .port(0)
            .handle((request, response) -> {
                requestedPath.set(requestedPath(request));
                return response.status(status)
                    .header("Content-Type", "application/json")
                    .sendString(Mono.just(body));
            })
            .bindNow();
    }

    private String requestedPath(HttpServerRequest request) {
        return request.uri();
    }

    private JsonPlaceholderClient client() {
        WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:" + server.port())
            .build();

        return new JsonPlaceholderClient(webClient);
    }
}
