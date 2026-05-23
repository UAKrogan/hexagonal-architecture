package com.example.hexagonal.application.service;

import com.example.hexagonal.application.port.in.GetContentUseCase;
import com.example.hexagonal.application.port.in.command.GetContentCommand;
import com.example.hexagonal.application.port.in.result.GetContentResult;
import org.springframework.stereotype.Service;

@Service
public class ContentApplicationService implements GetContentUseCase {

    @Override
    public GetContentResult getContent(GetContentCommand command) {
        return new GetContentResult(command.contentId(), "Temporary title", "Temporary content", 1L, command.provider());
    }
}
