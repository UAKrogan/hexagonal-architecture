package com.example.hexagonal.infrastructure.observability.context;

import com.example.hexagonal.infrastructure.http.propagation.HeaderPropagationProperties;
import com.example.hexagonal.infrastructure.observability.logging.RequestContextMdcThreadLocalAccessor;
import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
@EnableConfigurationProperties(HeaderPropagationProperties.class)
public class ReactorContextPropagationConfiguration {

    private final RequestContextMdcThreadLocalAccessor requestContextMdcThreadLocalAccessor =
        new RequestContextMdcThreadLocalAccessor();

    @Bean
    public RequestContextMdcThreadLocalAccessor requestContextMdcThreadLocalAccessor() {
        return requestContextMdcThreadLocalAccessor;
    }

    @PostConstruct
    void enableAutomaticContextPropagation() {
        ContextRegistry contextRegistry = ContextRegistry.getInstance();
        contextRegistry.removeThreadLocalAccessor(RequestContextMdcThreadLocalAccessor.KEY);
        contextRegistry.registerThreadLocalAccessor(requestContextMdcThreadLocalAccessor);
        Hooks.enableAutomaticContextPropagation();
    }
}
