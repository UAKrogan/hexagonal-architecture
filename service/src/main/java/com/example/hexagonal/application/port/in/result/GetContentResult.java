package com.example.hexagonal.application.port.in.result;

public record GetContentResult(Long id, String title, String content, Long authorId, String source) {
}
