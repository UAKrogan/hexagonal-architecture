package com.example.hexagonal.application.port.in.command;

import com.example.hexagonal.domain.model.ContentProviderType;

public record GetContentCommand(Long contentId, ContentProviderType provider) {
}