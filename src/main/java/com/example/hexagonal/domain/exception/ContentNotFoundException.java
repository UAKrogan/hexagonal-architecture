package com.example.hexagonal.domain.exception;

public class ContentNotFoundException extends RuntimeException {

    public ContentNotFoundException(String message) {
        super(message);
    }
}
