package com.example.hexagonal.infrastructure.observability.context;

import com.example.hexagonal.test.tag.UnitTest;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Map;

@UnitTest
class ReactorRequestContextAccessorTest {

    private final ReactorRequestContextAccessor requestContextAccessor = new ReactorRequestContextAccessor();

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
