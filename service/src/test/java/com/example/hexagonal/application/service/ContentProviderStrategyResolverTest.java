package com.example.hexagonal.application.service;

import com.example.hexagonal.domain.model.ContentProviderType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContentProviderStrategyResolverTest {

    @Test
    void shouldResolveStrategyByProviderType() {
        ContentProviderStrategy jsonPlaceholderStrategy = mock(ContentProviderStrategy.class);
        ContentProviderStrategy dummyJsonStrategy = mock(ContentProviderStrategy.class);

        when(jsonPlaceholderStrategy.getProviderType()).thenReturn(ContentProviderType.JSONPLACEHOLDER);
        when(dummyJsonStrategy.getProviderType()).thenReturn(ContentProviderType.DUMMYJSON);

        ContentProviderStrategyResolver resolver =
            new ContentProviderStrategyResolver(List.of(jsonPlaceholderStrategy, dummyJsonStrategy));

        assertThat(resolver.resolve(ContentProviderType.JSONPLACEHOLDER)).isSameAs(jsonPlaceholderStrategy);
        assertThat(resolver.resolve(ContentProviderType.DUMMYJSON)).isSameAs(dummyJsonStrategy);
    }

    @Test
    void shouldReturnNullWhenStrategyIsNotRegistered() {
        ContentProviderStrategyResolver resolver = new ContentProviderStrategyResolver(List.of());

        assertThat(resolver.resolve(ContentProviderType.JSONPLACEHOLDER)).isNull();
    }
}
