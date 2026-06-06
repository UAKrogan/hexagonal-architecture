package com.example.hexagonal.adapter.in.web.error;

import org.springframework.http.HttpStatus;

record ErrorResponse(HttpStatus status, Object body, boolean expected) {
}
