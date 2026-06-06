package com.example.hexagonal.adapter.out.dummyjson.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DummyJsonWebClientConfiguration {

    @Bean
    public WebClient dummyJsonWebClient(WebClient.Builder webClientBuilder,
                                        @Value("${application.http.clients.dummy-json.base-url:https://dummyjson.com}")
                                        String baseUrl) {
        return webClientBuilder
            .baseUrl(baseUrl)
            .build();
    }
}
