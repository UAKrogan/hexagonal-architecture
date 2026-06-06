package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.in.command.GetContentCommand;
import com.example.hexagonal.domain.exception.ContentNotFoundException;
import com.example.hexagonal.domain.model.Content;
import com.example.hexagonal.domain.model.ContentProviderType;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentApplicationServiceTest {

    private final ContentProviderStrategyResolver strategyResolver = mock(ContentProviderStrategyResolver.class);
    private final ContentProviderStrategy contentProviderStrategy = mock(ContentProviderStrategy.class);
    private final ContentApplicationService contentApplicationService = new ContentApplicationService(strategyResolver);

    @Test
    void shouldResolveProviderAndMapDomainContentToUseCaseResult() {
        GetContentCommand command = new GetContentCommand(1L, ContentProviderType.JSONPLACEHOLDER);
        Content content = new Content(1L, "title", "body", 10L, "JSONPLACEHOLDER");

        when(strategyResolver.resolve(ContentProviderType.JSONPLACEHOLDER)).thenReturn(contentProviderStrategy);
        when(contentProviderStrategy.getContent(1L)).thenReturn(Mono.just(content));

        StepVerifier.create(contentApplicationService.getContent(command))
            .expectNextMatches(result ->
                result.id().equals(1L)
                    && result.title().equals("title")
                    && result.content().equals("body")
                    && result.authorId().equals(10L)
                    && result.source().equals("JSONPLACEHOLDER")
            )
            .verifyComplete();

        verify(strategyResolver).resolve(ContentProviderType.JSONPLACEHOLDER);
        verify(contentProviderStrategy).getContent(1L);
    }

    @Test
    void shouldPropagateProviderErrors() {
        GetContentCommand command = new GetContentCommand(99L, ContentProviderType.DUMMYJSON);
        ContentNotFoundException exception = new ContentNotFoundException("not found");

        when(strategyResolver.resolve(ContentProviderType.DUMMYJSON)).thenReturn(contentProviderStrategy);
        when(contentProviderStrategy.getContent(99L)).thenReturn(Mono.error(exception));

        StepVerifier.create(contentApplicationService.getContent(command))
            .expectErrorSatisfies(error -> error.equals(exception))
            .verify();

        verify(strategyResolver).resolve(ContentProviderType.DUMMYJSON);
        verify(contentProviderStrategy).getContent(99L);
    }
}
