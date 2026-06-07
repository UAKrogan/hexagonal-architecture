package com.example.hexagonal.infrastructure.configuration;

import com.example.hexagonal.application.error.ExceptionToErrorCodeResolver;
import com.example.hexagonal.application.port.out.ContentProviderStrategyPort;
import com.example.hexagonal.application.service.ContentApplicationService;
import com.example.hexagonal.application.service.ContentProviderStrategyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ApplicationServiceConfiguration {

    @Bean
    ContentProviderStrategyResolver contentProviderStrategyResolver(
        List<ContentProviderStrategyPort> contentProviderStrategies
    ) {
        return new ContentProviderStrategyResolver(contentProviderStrategies);
    }

    @Bean
    ContentApplicationService contentApplicationService(ContentProviderStrategyResolver strategyResolver) {
        return new ContentApplicationService(strategyResolver);
    }

    @Bean
    ExceptionToErrorCodeResolver exceptionToErrorCodeResolver() {
        return new ExceptionToErrorCodeResolver();
    }
}
