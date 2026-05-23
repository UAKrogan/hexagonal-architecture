package com.example.hexagonal.adapter.in.web.mapper;

import com.example.hexagonal.adapter.in.web.dto.request.GetContentWebRequest;
import com.example.hexagonal.adapter.in.web.dto.response.GetContentWebResponse;
import com.example.hexagonal.application.port.in.command.GetContentCommand;
import com.example.hexagonal.application.port.in.result.GetContentResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentWebMapper {

    GetContentCommand toCommand(GetContentWebRequest request);

    GetContentWebResponse toResponse(GetContentResult result);
}
