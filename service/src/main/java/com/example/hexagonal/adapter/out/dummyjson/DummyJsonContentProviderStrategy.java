package com.example.hexagonal.adapter.out.dummyjson;

import com.example.hexagonal.application.service.ContentProviderStrategy;
import com.example.hexagonal.domain.model.Content;
import com.example.hexagonal.domain.model.ContentProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DummyJsonContentProviderStrategy implements ContentProviderStrategy {

    private final DummyJsonContentProviderAdapter dummyJsonContentProviderAdapter;

    @Override
    public ContentProviderType getProviderType() {
        return ContentProviderType.DUMMYJSON;
    }

    @Override
    public Mono<Content> getContent(Long contentId) {
        return dummyJsonContentProviderAdapter.getContent(contentId);
    }
}
