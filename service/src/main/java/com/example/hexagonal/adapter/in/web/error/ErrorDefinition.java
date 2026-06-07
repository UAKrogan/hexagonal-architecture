package com.example.hexagonal.adapter.in.web.error;

import org.springframework.http.HttpStatus;

interface ErrorDefinition {

    ErrorCode code();

    HttpStatus status();

    String system();

    String type();

    String title();

    ErrorResponseBody responseBody();

    boolean expected();
}
