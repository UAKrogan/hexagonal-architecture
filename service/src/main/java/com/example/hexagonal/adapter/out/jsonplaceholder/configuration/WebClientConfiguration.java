package com.example.hexagonal.adapter.out.jsonplaceholder.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder,
                               @Value("${application.http.clients.json-placeholder.base-url:https://jsonplaceholder.typicode.com}")
                               String baseUrl) {
        return webClientBuilder
            .baseUrl(baseUrl)
            .build();
    }
}
