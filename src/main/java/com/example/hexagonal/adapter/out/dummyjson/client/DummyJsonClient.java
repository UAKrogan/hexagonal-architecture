package com.example.hexagonal.adapter.out.dummyjson.client;

import com.example.hexagonal.adapter.out.dummyjson.dto.DummyJsonContentResponse;
import com.example.hexagonal.domain.exception.ContentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyJsonClient {

    private final WebClient dummyJsonWebClient;

    public Mono<DummyJsonContentResponse> getContent(Long contentId) {
        return dummyJsonWebClient.get()
            .uri("/posts/{id}", contentId)
            .retrieve()
            .onStatus(
                status -> status == HttpStatus.NOT_FOUND,
                response -> Mono.error(new ContentNotFoundException("Content not found for id: " + contentId))
            )
            .bodyToMono(DummyJsonContentResponse.class)
            .doOnSubscribe(subscription ->
                log.info(
                    "Calling DummyJSON API: contentId={}",
                    contentId
                )
            )
            .doOnNext(response ->
                log.info(
                    "Successfully received DummyJSON response: id={}",
                    response.id()
                )
            )
            .doOnError(ex ->
                log.error(
                    "Failed to retrieve content from JSONPlaceholder: contentId={}",
                    contentId,
                    ex
                )
            );
    }
}
