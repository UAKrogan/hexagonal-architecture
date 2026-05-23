package com.example.hexagonal.application.port.out;

import com.example.hexagonal.domain.model.Content;
import reactor.core.publisher.Mono;

public interface ContentProviderPort {

    Mono<Content> getContent(Long contentId);
}
