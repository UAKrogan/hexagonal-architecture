package com.example.hexagonal.adapter.out.jsonplaceholder.client;

import com.example.hexagonal.adapter.out.jsonplaceholder.dto.JsonPlaceholderContentResponse;
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
public class JsonPlaceholderClient {

    private final WebClient webClient;

    public Mono<JsonPlaceholderContentResponse> getContent(Long contentId) {
        return webClient.get()
            .uri("/posts/{id}", contentId)
            .retrieve()
            .onStatus(
                status -> status == HttpStatus.NOT_FOUND,
                response -> Mono.error(new ContentNotFoundException("Content not found for id: " + contentId))
            )
            .bodyToMono(JsonPlaceholderContentResponse.class)
            .doOnSubscribe(subscription ->
                log.info(
                    "Calling JSONPlaceholder API: contentId={}",
                    contentId
                )
            )
            .doOnNext(response ->
                log.info(
                    "Successfully received JSONPlaceholder response: id={}",
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
