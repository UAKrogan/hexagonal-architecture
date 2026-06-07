package com.example.hexagonal.infrastructure.observability.context;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReactorRequestContextAccessor {

    public Mono<RequestContext> current() {
        return Mono.deferContextual(contextView ->
            Mono.justOrEmpty(contextView.getOrEmpty(RequestContext.class))
        );
    }
}
