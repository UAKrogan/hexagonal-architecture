package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.in.GetContentUseCase;
import com.example.hexagonal.application.port.in.command.GetContentCommand;
import com.example.hexagonal.application.port.in.result.GetContentResult;
import com.example.hexagonal.application.port.out.ContentProviderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentApplicationService implements GetContentUseCase {

    private final ContentProviderPort contentProviderPort;

    @Override
    public Mono<GetContentResult> getContent(GetContentCommand command) {
        return contentProviderPort.getContent(command.contentId())
            .doOnNext(content ->
                log.info(
                    "Successfully retrieved content from provider: id={}, source={}",
                    content.id(),
                    content.source()
                )
            )
            .map(content -> new GetContentResult(
                content.id(),
                content.title(),
                content.content(),
                content.authorId(),
                content.source()
            ))
            .doOnSuccess(result ->
                log.info(
                    "Successfully mapped content result: id={}, source={}",
                    result.id(),
                    result.source()
                )
            )
            .doOnError(ex ->
                log.error(
                    "Failed to execute get content use case: contentId={}, provider={}",
                    command.contentId(),
                    command.provider(),
                    ex
                )
            );
    }
}
