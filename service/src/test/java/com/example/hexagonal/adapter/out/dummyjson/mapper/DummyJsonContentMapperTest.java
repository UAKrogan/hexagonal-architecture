package com.example.hexagonal.adapter.out.dummyjson.mapper;

import com.example.hexagonal.adapter.out.dummyjson.dto.DummyJsonContentResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class DummyJsonContentMapperTest {

    private final DummyJsonContentMapper mapper = Mappers.getMapper(DummyJsonContentMapper.class);

    @Test
    void shouldMapDummyJsonResponseToDomainContent() {
        DummyJsonContentResponse response = new DummyJsonContentResponse(1L, "title", "body", 10L);

        var content = mapper.toDomain(response);

        assertThat(content.id()).isEqualTo(1L);
        assertThat(content.title()).isEqualTo("title");
        assertThat(content.content()).isEqualTo("body");
        assertThat(content.authorId()).isEqualTo(10L);
        assertThat(content.source()).isEqualTo("DUMMYJSON");
    }
}
