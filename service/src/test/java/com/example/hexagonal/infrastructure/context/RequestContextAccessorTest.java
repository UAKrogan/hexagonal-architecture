package com.example.hexagonal.infrastructure.context;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Map;

class RequestContextAccessorTest {

    private final RequestContextAccessor requestContextAccessor = new RequestContextAccessor();

    @Test
    void shouldReadRequestContextFromReactorContext() {
        RequestContext requestContext = new RequestContext("correlation-id", "1", Map.of());

        StepVerifier.create(requestContextAccessor.current()
                .contextWrite(context -> context.put(RequestContext.class, requestContext)))
            .expectNext(requestContext)
            .verifyComplete();
    }

    @Test
    void shouldCompleteEmptyWhenRequestContextIsMissing() {
        StepVerifier.create(requestContextAccessor.current())
            .verifyComplete();
    }
}
