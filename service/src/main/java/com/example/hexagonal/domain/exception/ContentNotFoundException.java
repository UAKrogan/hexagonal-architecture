package com.example.hexagonal.domain.exception;

public class ContentNotFoundException extends DomainException {

    public ContentNotFoundException(String message) {
        super(message);
    }
}
