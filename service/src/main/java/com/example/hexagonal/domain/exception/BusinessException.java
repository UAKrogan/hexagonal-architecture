package com.example.hexagonal.domain.exception;

public class BusinessException extends DomainException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
