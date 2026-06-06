package com.example.hexagonal.adapter.out.dummyjson;

import com.example.hexagonal.test.tag.UnitTest;
import com.example.hexagonal.adapter.out.dummyjson.client.DummyJsonClient;
import com.example.hexagonal.adapter.out.dummyjson.dto.DummyJsonContentResponse;
import com.example.hexagonal.adapter.out.dummyjson.mapper.DummyJsonContentMapper;
import com.example.hexagonal.domain.exception.ContentNotFoundException;
import com.example.hexagonal.domain.model.Content;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
class DummyJsonContentProviderAdapterTest {

    private final DummyJsonClient dummyJsonClient = mock(DummyJsonClient.class);
    private final DummyJsonContentMapper dummyJsonContentMapper = mock(DummyJsonContentMapper.class);
    private final DummyJsonContentProviderAdapter adapter =
        new DummyJsonContentProviderAdapter(dummyJsonClient, dummyJsonContentMapper);

    @Test
    void shouldRetrieveAndMapContent() {
        DummyJsonContentResponse response = new DummyJsonContentResponse(1L, "title", "body", 10L);
        Content content = new Content(1L, "title", "body", 10L, "DUMMYJSON");

        when(dummyJsonClient.getContent(1L)).thenReturn(Mono.just(response));
        when(dummyJsonContentMapper.toDomain(response)).thenReturn(content);

        StepVerifier.create(adapter.getContent(1L))
            .expectNext(content)
            .verifyComplete();

        verify(dummyJsonClient).getContent(1L);
        verify(dummyJsonContentMapper).toDomain(response);
    }

    @Test
    void shouldPropagateClientErrors() {
        ContentNotFoundException exception = new ContentNotFoundException("not found");

        when(dummyJsonClient.getContent(99L)).thenReturn(Mono.error(exception));

        StepVerifier.create(adapter.getContent(99L))
            .expectErrorSatisfies(error -> error.equals(exception))
            .verify();

        verify(dummyJsonClient).getContent(99L);
    }
}
