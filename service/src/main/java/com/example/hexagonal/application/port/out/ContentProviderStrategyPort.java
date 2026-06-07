package com.example.hexagonal.application.port.out;

import com.example.hexagonal.domain.model.Content;
import com.example.hexagonal.domain.model.ContentProviderType;
import reactor.core.publisher.Mono;

public interface ContentProviderStrategyPort {

    ContentProviderType getProviderType();

    Mono<Content> getContent(Long contentId);
}
