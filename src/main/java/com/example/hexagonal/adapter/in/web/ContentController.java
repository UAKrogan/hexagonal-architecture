package com.example.hexagonal.adapter.in.web;

import com.example.hexagonal.adapter.in.web.dto.request.GetContentWebRequest;
import com.example.hexagonal.adapter.in.web.dto.response.GetContentWebResponse;
import com.example.hexagonal.adapter.in.web.mapper.ContentWebMapper;
import com.example.hexagonal.application.port.in.GetContentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final GetContentUseCase getContentUseCase;
    private final ContentWebMapper contentWebMapper;

    @PostMapping
    public Mono<GetContentWebResponse> getContent(@RequestBody GetContentWebRequest request) {
        log.info(
            "Received get content request: contentId={}, provider={}",
            request.contentId(),
            request.provider()
        );

        return getContentUseCase.getContent(contentWebMapper.toCommand(request))
            .map(contentWebMapper::toResponse)
            .doOnSuccess(res ->
                log.info(
                    "Successfully processed content request: id={}, source={}",
                    res.id(),
                    res.source()
                )
            )
            .doOnError(ex ->
                log.error(
                    "Failed to process content request: contentId={}, provider={}",
                    request.contentId(),
                    request.provider(),
                    ex
                )
            );
    }
}
