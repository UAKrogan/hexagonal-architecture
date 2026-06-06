package com.example.hexagonal.adapter.in.web.mapper;

import com.example.hexagonal.test.tag.UnitTest;
import com.example.hexagonal.application.port.in.result.GetContentResult;
import com.example.hexagonal.contract.model.ContentProviderDto;
import com.example.hexagonal.contract.model.GetContentRequestDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class ContentWebMapperTest {

    private final ContentWebMapper contentWebMapper = Mappers.getMapper(ContentWebMapper.class);

    @Test
    void shouldMapRequestDtoToCommand() {
        GetContentRequestDto dto = new GetContentRequestDto(1L, ContentProviderDto.JSONPLACEHOLDER);

        var command = contentWebMapper.toCommand(dto);

        assertThat(command.contentId()).isEqualTo(1L);
        assertThat(command.provider().name()).isEqualTo("JSONPLACEHOLDER");
    }

    @Test
    void shouldMapUseCaseResultToResponseDto() {
        GetContentResult result = new GetContentResult(1L, "title", "body", 10L, "DUMMYJSON");

        var dto = contentWebMapper.toDto(result);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("title");
        assertThat(dto.getContent()).isEqualTo("body");
        assertThat(dto.getAuthorId()).isEqualTo(10L);
        assertThat(dto.getSource()).isEqualTo(ContentProviderDto.DUMMYJSON);
    }
}
