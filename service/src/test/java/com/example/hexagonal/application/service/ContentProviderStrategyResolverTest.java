package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.out.ContentProviderStrategyPort;
import com.example.hexagonal.domain.model.ContentProviderType;
import com.example.hexagonal.test.tag.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
class ContentProviderStrategyResolverTest {

    @Test
    void shouldResolveStrategyByProviderType() {
        ContentProviderStrategyPort jsonPlaceholderStrategy = mock(ContentProviderStrategyPort.class);
        ContentProviderStrategyPort dummyJsonStrategy = mock(ContentProviderStrategyPort.class);

        when(jsonPlaceholderStrategy.getProviderType()).thenReturn(ContentProviderType.JSONPLACEHOLDER);
        when(dummyJsonStrategy.getProviderType()).thenReturn(ContentProviderType.DUMMYJSON);

        ContentProviderStrategyResolver resolver =
            new ContentProviderStrategyResolver(List.of(jsonPlaceholderStrategy, dummyJsonStrategy));

        assertThat(resolver.resolve(ContentProviderType.JSONPLACEHOLDER)).isSameAs(jsonPlaceholderStrategy);
        assertThat(resolver.resolve(ContentProviderType.DUMMYJSON)).isSameAs(dummyJsonStrategy);
    }

    @Test
    void shouldFailFastWhenStrategyIsNotRegistered() {
        assertThatThrownBy(() -> new ContentProviderStrategyResolver(List.of()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Missing content provider strategies")
            .hasMessageContaining(ContentProviderType.JSONPLACEHOLDER.name())
            .hasMessageContaining(ContentProviderType.DUMMYJSON.name());
    }
}
