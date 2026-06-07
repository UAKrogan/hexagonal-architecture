package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.in.GetContentUseCase;
import com.example.hexagonal.application.port.in.command.GetContentCommand;
import com.example.hexagonal.application.port.in.result.GetContentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ContentApplicationService implements GetContentUseCase {

    private final ContentProviderStrategyResolver strategyResolver;

    @Override
    public Mono<GetContentResult> getContent(GetContentCommand command) {
        return strategyResolver.resolve(command.provider())
            .getContent(command.contentId())
            .map(content -> new GetContentResult(
                content.id(),
                content.title(),
                content.content(),
                content.authorId(),
                content.source()
            ));
    }
}
