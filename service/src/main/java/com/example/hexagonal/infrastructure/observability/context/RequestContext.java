package com.example.hexagonal.infrastructure.observability.context;

import java.util.Map;
import java.util.UUID;

public record RequestContext(UUID correlationId, String apiVersion, Map<String, String> headers) {

    public RequestContext {
        headers = Map.copyOf(headers);
    }
}
