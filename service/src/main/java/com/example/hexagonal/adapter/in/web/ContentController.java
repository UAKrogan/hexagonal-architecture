package com.example.hexagonal.adapter.in.web;

import com.example.hexagonal.adapter.in.web.mapper.ContentWebMapper;
import com.example.hexagonal.application.port.in.GetContentUseCase;
import com.example.hexagonal.contract.api.ContentApi;
import com.example.hexagonal.contract.model.GetContentRequestDto;
import com.example.hexagonal.contract.model.GetContentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ContentController implements ContentApi {

    private final GetContentUseCase getContentUseCase;
    private final ContentWebMapper contentWebMapper;

    @Override
    public Mono<GetContentResponseDto> getContent(String xApiVersion,
                                                  UUID xCorrelationId,
                                                  Mono<GetContentRequestDto> getContentRequestDto,
                                                  ServerWebExchange exchange) {

        return getContentRequestDto
            .doOnNext(request ->
                log.info(
                    "Received get content request: apiVersion={}, correlationId={}, contentId={}, provider={}",
                    xApiVersion,
                    xCorrelationId,
                    request.getContentId(),
                    request.getProvider()
                )
            )
            .map(contentWebMapper::toCommand)
            .flatMap(getContentUseCase::getContent)
            .map(contentWebMapper::toDto)
            .doOnSuccess(response -> {

                if (response != null) {

                    log.info(
                        "Successfully processed content request: apiVersion={}, correlationId={}, id={}, source={}",
                        xApiVersion,
                        xCorrelationId,
                        response.getId(),
                        response.getSource()
                    );
                }
            })
            .doOnError(ex ->
                log.error(
                    "Failed to process content request: apiVersion={}, correlationId={}",
                    xApiVersion,
                    xCorrelationId,
                    ex
                )
            );
    }
}
