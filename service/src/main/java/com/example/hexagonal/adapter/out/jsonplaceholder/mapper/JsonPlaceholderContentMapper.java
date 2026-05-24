package com.example.hexagonal.adapter.out.jsonplaceholder.mapper;

import com.example.hexagonal.adapter.out.jsonplaceholder.dto.JsonPlaceholderContentResponse;
import com.example.hexagonal.domain.model.Content;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JsonPlaceholderContentMapper {

    @Mapping(target = "content", source = "body")
    @Mapping(target = "authorId", source = "userId")
    @Mapping(target = "source", constant = "JSONPLACEHOLDER")
    Content toDomain(JsonPlaceholderContentResponse response);
}
