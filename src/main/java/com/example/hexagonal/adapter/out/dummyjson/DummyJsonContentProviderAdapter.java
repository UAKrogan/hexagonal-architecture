package com.example.hexagonal.adapter.out.dummyjson;

import com.example.hexagonal.adapter.out.dummyjson.client.DummyJsonClient;
import com.example.hexagonal.adapter.out.dummyjson.mapper.DummyJsonContentMapper;
import com.example.hexagonal.application.port.out.ContentProviderPort;
import com.example.hexagonal.domain.model.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DummyJsonContentProviderAdapter implements ContentProviderPort {

    private final DummyJsonClient dummyJsonClient;
    private final DummyJsonContentMapper dummyJsonContentMapper;

    @Override
    public Mono<Content> getContent(Long contentId) {
        return dummyJsonClient.getContent(contentId)
            .map(dummyJsonContentMapper::toDomain);
    }
}
