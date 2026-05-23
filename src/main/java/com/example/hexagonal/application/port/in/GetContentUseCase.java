package com.example.hexagonal.application.port.in;

import com.example.hexagonal.application.port.in.command.GetContentCommand;
import com.example.hexagonal.application.port.in.result.GetContentResult;
import reactor.core.publisher.Mono;

public interface GetContentUseCase {

    Mono<GetContentResult> getContent(GetContentCommand command);
}
