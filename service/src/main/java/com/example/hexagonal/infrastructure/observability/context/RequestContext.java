package com.example.hexagonal.infrastructure.observability.context;

import java.util.Map;

public record RequestContext(String correlationId, String apiVersion, Map<String, String> headers) {

    public RequestContext {
        headers = Map.copyOf(headers);
    }
}
