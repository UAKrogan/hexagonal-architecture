package com.example.hexagonal.infrastructure.http.propagation;

import com.example.hexagonal.infrastructure.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class HeaderPropagationExchangeFilterFunction implements ExchangeFilterFunction {

    private final HeaderPropagationProperties headerPropagationProperties;

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return Mono.deferContextual(contextView -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(request);

            contextView.<RequestContext>getOrEmpty(RequestContext.class)
                .ifPresent(requestContext -> headerPropagationProperties.normalizedHeaders()
                    .forEach(headerName -> propagateHeader(requestBuilder, requestContext, headerName)));

            return next.exchange(requestBuilder.build());
        });
    }

    private void propagateHeader(ClientRequest.Builder requestBuilder,
                                 RequestContext requestContext,
                                 String headerName) {

        String value = requestContext.headers().get(headerName);
        if (value != null) {
            requestBuilder.headers(headers -> headers.set(headerName, value));
        }
    }
}
