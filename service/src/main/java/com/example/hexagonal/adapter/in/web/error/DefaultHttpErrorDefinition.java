package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.application.error.ErrorCode;
import org.springframework.http.HttpStatus;

record DefaultHttpErrorDefinition(
    ErrorCode code,
    HttpStatus status,
    String system,
    String type,
    String title,
    ErrorResponseBody responseBody,
    boolean expected
) implements HttpErrorDefinition {
}
