package com.example.hexagonal.bootstrap.configuration;

import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ApiVersionConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.useRequestHeader("x-api-version");
    }
}
