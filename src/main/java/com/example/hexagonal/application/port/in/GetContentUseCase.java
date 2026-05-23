package com.example.hexagonal.application.port.in;

import com.example.hexagonal.application.port.in.command.GetContentCommand;
import com.example.hexagonal.application.port.in.result.GetContentResult;

public interface GetContentUseCase {

    GetContentResult getContent(GetContentCommand command);

}
