package com.example.hexagonal.infrastructure.observability.context;

public final class RequestContextHeaders {

    public static final String CORRELATION_ID = "x-correlation-id";
    public static final String API_VERSION = "x-api-version";

    private RequestContextHeaders() {
    }
}
