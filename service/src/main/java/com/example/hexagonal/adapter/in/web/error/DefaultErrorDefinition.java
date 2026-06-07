package com.example.hexagonal.adapter.in.web.error;

import org.springframework.http.HttpStatus;

record DefaultErrorDefinition(
    ErrorCode code,
    HttpStatus status,
    String system,
    String type,
    String title,
    ErrorResponseBody responseBody,
    boolean expected
) implements ErrorDefinition {
}
