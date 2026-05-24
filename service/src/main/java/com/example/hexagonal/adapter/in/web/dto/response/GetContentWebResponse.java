package com.example.hexagonal.adapter.in.web.dto.response;

public record GetContentWebResponse(Long id, String title, String content, Long authorId, String source) {
}
