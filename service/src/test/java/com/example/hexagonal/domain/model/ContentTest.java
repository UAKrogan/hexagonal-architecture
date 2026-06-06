package com.example.hexagonal.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTest {

    @Test
    void shouldExposeImmutableContentValuesAndEqualitySemantics() {
        Content content = new Content(1L, "title", "body", 10L, "JSONPLACEHOLDER");
        Content sameContent = new Content(1L, "title", "body", 10L, "JSONPLACEHOLDER");
        Content differentContent = new Content(2L, "other-title", "other-body", 11L, "DUMMYJSON");

        assertThat(content)
            .isEqualTo(sameContent)
            .hasSameHashCodeAs(sameContent)
            .isNotEqualTo(differentContent);

        assertThat(content.id()).isEqualTo(1L);
        assertThat(content.title()).isEqualTo("title");
        assertThat(content.content()).isEqualTo("body");
        assertThat(content.authorId()).isEqualTo(10L);
        assertThat(content.source()).isEqualTo("JSONPLACEHOLDER");
    }
}
