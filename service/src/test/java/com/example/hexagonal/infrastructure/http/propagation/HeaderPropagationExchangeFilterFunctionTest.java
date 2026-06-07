package com.example.hexagonal.infrastructure.http.propagation;

import com.example.hexagonal.test.tag.UnitTest;
import com.example.hexagonal.infrastructure.observability.context.RequestContext;
import com.example.hexagonal.infrastructure.observability.context.RequestContextHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
class HeaderPropagationExchangeFilterFunctionTest {

    private final HeaderPropagationProperties properties = new HeaderPropagationProperties(
        List.of(RequestContextHeaders.CORRELATION_ID, RequestContextHeaders.API_VERSION, "x-allowed")
    );
    private final HeaderPropagationExchangeFilterFunction filterFunction =
        new HeaderPropagationExchangeFilterFunction(properties);

    @Test
    void shouldPropagateOnlyConfiguredHeadersFromRequestContext() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ClientResponse response = mock(ClientResponse.class);
        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);
            return Mono.just(response);
        };
        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("http://example.com"))
            .header("x-not-allowed", "original-value")
            .build();
        RequestContext requestContext = new RequestContext(
            "correlation-id",
            "1",
            Map.of(
                RequestContextHeaders.CORRELATION_ID, "correlation-id",
                RequestContextHeaders.API_VERSION, "1",
                "x-allowed", "allowed-value",
                "x-not-allowed", "not-allowed-value"
            )
        );

        StepVerifier.create(filterFunction.filter(request, exchangeFunction)
                .contextWrite(context -> context.put(RequestContext.class, requestContext)))
            .expectNext(response)
            .verifyComplete();

        assertThat(capturedRequest.get().headers().getFirst(RequestContextHeaders.CORRELATION_ID))
            .isEqualTo("correlation-id");
        assertThat(capturedRequest.get().headers().getFirst(RequestContextHeaders.API_VERSION))
            .isEqualTo("1");
        assertThat(capturedRequest.get().headers().getFirst("x-allowed")).isEqualTo("allowed-value");
        assertThat(capturedRequest.get().headers().getFirst("x-not-allowed")).isEqualTo("original-value");
    }

    @Test
    void shouldLeaveRequestUnchangedWhenRequestContextIsMissing() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ClientResponse response = mock(ClientResponse.class);
        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);
            return Mono.just(response);
        };
        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("http://example.com"))
            .build();

        StepVerifier.create(filterFunction.filter(request, exchangeFunction))
            .expectNext(response)
            .verifyComplete();

        assertThat(capturedRequest.get().headers().getFirst(RequestContextHeaders.CORRELATION_ID)).isNull();
        assertThat(capturedRequest.get().headers().getFirst(RequestContextHeaders.API_VERSION)).isNull();
    }

    @Test
    void shouldUseLatestContextHeaderValueWhenHeaderAlreadyExists() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ClientResponse response = mock(ClientResponse.class);
        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);
            return Mono.just(response);
        };
        ClientRequest request = ClientRequest.create(org.springframework.http.HttpMethod.GET, URI.create("http://example.com"))
            .header(RequestContextHeaders.CORRELATION_ID, "old-value")
            .build();
        RequestContext requestContext = new RequestContext(
            "new-value",
            "1",
            Map.of(
                RequestContextHeaders.CORRELATION_ID, "new-value",
                RequestContextHeaders.API_VERSION, "1"
            )
        );

        when(response.releaseBody()).thenReturn(Mono.empty());

        StepVerifier.create(filterFunction.filter(request, exchangeFunction)
                .contextWrite(context -> context.put(RequestContext.class, requestContext)))
            .expectNext(response)
            .verifyComplete();

        assertThat(capturedRequest.get().headers().get(RequestContextHeaders.CORRELATION_ID))
            .containsExactly("new-value");
    }
}
