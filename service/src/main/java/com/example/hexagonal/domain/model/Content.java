package com.example.hexagonal.domain.model;

public record Content(Long id, String title, String content, Long authorId, String source) {
}
