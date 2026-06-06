package com.example.hexagonal.infrastructure.http.propagation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class HeaderPropagationWebClientCustomizer implements WebClientCustomizer {

    private final HeaderPropagationExchangeFilterFunction headerPropagationExchangeFilterFunction;

    @Override
    public void customize(WebClient.Builder webClientBuilder) {
        webClientBuilder.filter(headerPropagationExchangeFilterFunction);
    }
}
