package com.example.hexagonal.adapter.out.jsonplaceholder;

import com.example.hexagonal.application.service.ContentProviderStrategy;
import com.example.hexagonal.domain.model.Content;
import com.example.hexagonal.domain.model.ContentProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JsonPlaceholderContentProviderStrategy implements ContentProviderStrategy {

    private final JsonPlaceholderContentProviderAdapter jsonPlaceholderContentProviderAdapter;

    @Override
    public ContentProviderType getProviderType() {
        return ContentProviderType.JSONPLACEHOLDER;
    }

    @Override
    public Mono<Content> getContent(Long contentId) {
        return jsonPlaceholderContentProviderAdapter.getContent(contentId);
    }
}
