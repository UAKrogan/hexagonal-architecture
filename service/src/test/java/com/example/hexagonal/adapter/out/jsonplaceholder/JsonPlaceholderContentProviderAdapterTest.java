package com.example.hexagonal.adapter.out.jsonplaceholder;

import com.example.hexagonal.adapter.out.jsonplaceholder.client.JsonPlaceholderClient;
import com.example.hexagonal.adapter.out.jsonplaceholder.dto.JsonPlaceholderContentResponse;
import com.example.hexagonal.adapter.out.jsonplaceholder.mapper.JsonPlaceholderContentMapper;
import com.example.hexagonal.domain.exception.ContentNotFoundException;
import com.example.hexagonal.domain.model.Content;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonPlaceholderContentProviderAdapterTest {

    private final JsonPlaceholderClient jsonPlaceholderClient = mock(JsonPlaceholderClient.class);
    private final JsonPlaceholderContentMapper jsonPlaceholderContentMapper = mock(JsonPlaceholderContentMapper.class);
    private final JsonPlaceholderContentProviderAdapter adapter =
        new JsonPlaceholderContentProviderAdapter(jsonPlaceholderClient, jsonPlaceholderContentMapper);

    @Test
    void shouldRetrieveAndMapContent() {
        JsonPlaceholderContentResponse response = new JsonPlaceholderContentResponse(10L, 1L, "title", "body");
        Content content = new Content(1L, "title", "body", 10L, "JSONPLACEHOLDER");

        when(jsonPlaceholderClient.getContent(1L)).thenReturn(Mono.just(response));
        when(jsonPlaceholderContentMapper.toDomain(response)).thenReturn(content);

        StepVerifier.create(adapter.getContent(1L))
            .expectNext(content)
            .verifyComplete();

        verify(jsonPlaceholderClient).getContent(1L);
        verify(jsonPlaceholderContentMapper).toDomain(response);
    }

    @Test
    void shouldPropagateClientErrors() {
        ContentNotFoundException exception = new ContentNotFoundException("not found");

        when(jsonPlaceholderClient.getContent(99L)).thenReturn(Mono.error(exception));

        StepVerifier.create(adapter.getContent(99L))
            .expectErrorSatisfies(error -> error.equals(exception))
            .verify();

        verify(jsonPlaceholderClient).getContent(99L);
    }
}
