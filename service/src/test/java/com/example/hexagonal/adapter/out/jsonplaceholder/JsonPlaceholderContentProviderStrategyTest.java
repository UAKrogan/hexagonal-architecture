package com.example.hexagonal.adapter.out.jsonplaceholder;

import com.example.hexagonal.test.tag.UnitTest;
import com.example.hexagonal.domain.model.Content;
import com.example.hexagonal.domain.model.ContentProviderType;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
class JsonPlaceholderContentProviderStrategyTest {

    private final JsonPlaceholderContentProviderAdapter adapter = mock(JsonPlaceholderContentProviderAdapter.class);
    private final JsonPlaceholderContentProviderStrategy strategy =
        new JsonPlaceholderContentProviderStrategy(adapter);

    @Test
    void shouldExposeProviderType() {
        assertThat(strategy.getProviderType()).isEqualTo(ContentProviderType.JSONPLACEHOLDER);
    }

    @Test
    void shouldDelegateContentRetrievalToAdapter() {
        Content content = new Content(1L, "title", "body", 10L, "JSONPLACEHOLDER");

        when(adapter.getContent(1L)).thenReturn(Mono.just(content));

        StepVerifier.create(strategy.getContent(1L))
            .expectNext(content)
            .verifyComplete();

        verify(adapter).getContent(1L);
    }
}
