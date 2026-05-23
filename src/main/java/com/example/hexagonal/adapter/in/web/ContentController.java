package com.example.hexagonal.adapter.in.web;

import com.example.hexagonal.adapter.in.web.dto.request.GetContentWebRequest;
import com.example.hexagonal.adapter.in.web.dto.response.GetContentWebResponse;
import com.example.hexagonal.adapter.in.web.mapper.ContentWebMapper;
import com.example.hexagonal.application.port.in.GetContentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final GetContentUseCase getContentUseCase;
    private final ContentWebMapper contentWebMapper;

    @PostMapping
    public Mono<GetContentWebResponse> getContent(@RequestBody GetContentWebRequest request) {
        return Mono.just(request)
            .map(contentWebMapper::toCommand)
            .map(getContentUseCase::getContent)
            .map(contentWebMapper::toResponse);
    }
}
