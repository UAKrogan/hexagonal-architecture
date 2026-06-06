package com.example.hexagonal.infrastructure.context;

import java.util.Map;

public record RequestContext(String correlationId, String apiVersion, Map<String, String> headers) {

    public RequestContext {
        headers = Map.copyOf(headers);
    }
}
