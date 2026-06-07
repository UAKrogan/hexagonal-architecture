package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.application.error.ErrorCode;
import org.springframework.http.HttpStatus;

interface HttpErrorDefinition {

    ErrorCode code();

    HttpStatus status();

    String system();

    String type();

    String title();

    ErrorResponseBody responseBody();

    boolean expected();
}
