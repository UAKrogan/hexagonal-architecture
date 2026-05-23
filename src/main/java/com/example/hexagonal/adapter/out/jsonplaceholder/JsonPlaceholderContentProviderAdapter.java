package com.example.hexagonal.adapter.out.jsonplaceholder;

import com.example.hexagonal.adapter.out.jsonplaceholder.client.JsonPlaceholderClient;
import com.example.hexagonal.adapter.out.jsonplaceholder.mapper.JsonPlaceholderContentMapper;
import com.example.hexagonal.application.port.out.ContentProviderPort;
import com.example.hexagonal.domain.model.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JsonPlaceholderContentProviderAdapter implements ContentProviderPort {

    private final JsonPlaceholderClient jsonPlaceholderClient;
    private final JsonPlaceholderContentMapper jsonPlaceholderContentMapper;

    @Override
    public Mono<Content> getContent(Long contentId) {
        return jsonPlaceholderClient.getContent(contentId)
            .map(jsonPlaceholderContentMapper::toDomain);
    }
}
