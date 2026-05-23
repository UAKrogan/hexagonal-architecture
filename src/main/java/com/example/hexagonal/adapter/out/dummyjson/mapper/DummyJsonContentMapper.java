package com.example.hexagonal.adapter.out.dummyjson.mapper;

import com.example.hexagonal.adapter.out.dummyjson.dto.DummyJsonContentResponse;
import com.example.hexagonal.domain.model.Content;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DummyJsonContentMapper {

    @Mapping(target = "content", source = "body")
    @Mapping(target = "authorId", source = "userId")
    @Mapping(target = "source", constant = "DUMMYJSON")
    Content toDomain(DummyJsonContentResponse response);
}
