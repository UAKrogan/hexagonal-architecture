package com.example.hexagonal.adapter.out.jsonplaceholder.mapper;

import com.example.hexagonal.test.tag.UnitTest;
import com.example.hexagonal.adapter.out.jsonplaceholder.dto.JsonPlaceholderContentResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class JsonPlaceholderContentMapperTest {

    private final JsonPlaceholderContentMapper mapper = Mappers.getMapper(JsonPlaceholderContentMapper.class);

    @Test
    void shouldMapJsonPlaceholderResponseToDomainContent() {
        JsonPlaceholderContentResponse response = new JsonPlaceholderContentResponse(10L, 1L, "title", "body");

        var content = mapper.toDomain(response);

        assertThat(content.id()).isEqualTo(1L);
        assertThat(content.title()).isEqualTo("title");
        assertThat(content.content()).isEqualTo("body");
        assertThat(content.authorId()).isEqualTo(10L);
        assertThat(content.source()).isEqualTo("JSONPLACEHOLDER");
    }
}
