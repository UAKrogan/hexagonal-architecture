package com.example.hexagonal.application.service;

import com.example.hexagonal.domain.model.Content;
import com.example.hexagonal.domain.model.ContentProviderType;
import reactor.core.publisher.Mono;

public interface ContentProviderStrategy {

    ContentProviderType getProviderType();

    Mono<Content> getContent(Long contentId);
}
