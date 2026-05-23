package com.example.hexagonal.application.port.in.command;

public record GetContentCommand(Long contentId, String provider) {
}