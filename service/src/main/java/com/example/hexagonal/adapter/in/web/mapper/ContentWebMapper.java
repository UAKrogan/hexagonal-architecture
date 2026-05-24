package com.example.hexagonal.adapter.in.web.mapper;

import com.example.hexagonal.application.port.in.command.GetContentCommand;
import com.example.hexagonal.application.port.in.result.GetContentResult;
import com.example.hexagonal.contract.model.GetContentRequestDto;
import com.example.hexagonal.contract.model.GetContentResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentWebMapper {

    GetContentCommand toCommand(GetContentRequestDto dto);

    GetContentResponseDto toDto(GetContentResult result);
}
