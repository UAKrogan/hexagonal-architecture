package com.example.hexagonal.infrastructure.observability.context;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class RequestContextFactory {

    private static final String UNKNOWN = "unknown";

    public RequestContext create(Map<String, String> inboundHeaders,
                                 Collection<String> propagatedHeaderNames,
                                 UUID fallbackCorrelationId) {

        Map<String, String> normalizedInboundHeaders = normalizeHeaders(inboundHeaders);
        UUID defaultCorrelationId = fallbackCorrelationId(fallbackCorrelationId);

        String correlationIdHeader = headerOrDefault(
            normalizedInboundHeaders,
            RequestContextHeaders.CORRELATION_ID,
            defaultCorrelationId.toString()
        );
        UUID correlationId = parseCorrelationId(correlationIdHeader, defaultCorrelationId);
        String apiVersion = headerOrDefault(
            normalizedInboundHeaders,
            RequestContextHeaders.API_VERSION,
            UNKNOWN
        );

        Map<String, String> propagatedHeaders = propagatedHeaders(
            normalizedInboundHeaders,
            propagatedHeaderNames
        );
        propagatedHeaders.put(RequestContextHeaders.CORRELATION_ID, correlationIdHeader);
        propagatedHeaders.put(RequestContextHeaders.API_VERSION, apiVersion);

        return new RequestContext(correlationId, apiVersion, propagatedHeaders);
    }

    private Map<String, String> normalizeHeaders(Map<String, String> headers) {
        Map<String, String> normalizedHeaders = new HashMap<>();

        if (headers == null) {
            return normalizedHeaders;
        }

        headers.forEach((name, value) -> {
            if (name != null) {
                normalizedHeaders.put(normalize(name), value);
            }
        });

        return normalizedHeaders;
    }

    private Map<String, String> propagatedHeaders(Map<String, String> inboundHeaders,
                                                  Collection<String> propagatedHeaderNames) {

        Map<String, String> propagatedHeaders = new HashMap<>();

        if (propagatedHeaderNames == null) {
            return propagatedHeaders;
        }

        for (String headerName : propagatedHeaderNames) {
            String normalizedHeaderName = normalize(headerName);
            String value = inboundHeaders.get(normalizedHeaderName);
            if (value != null) {
                propagatedHeaders.put(normalizedHeaderName, value);
            }
        }

        return propagatedHeaders;
    }

    private String headerOrDefault(Map<String, String> headers, String headerName, String defaultValue) {
        String value = headers.get(headerName);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private UUID fallbackCorrelationId(UUID fallbackCorrelationId) {
        return fallbackCorrelationId == null ? UUID.randomUUID() : fallbackCorrelationId;
    }

    private UUID parseCorrelationId(String correlationId, UUID defaultCorrelationId) {
        try {
            return UUID.fromString(correlationId);
        } catch (IllegalArgumentException ex) {
            return defaultCorrelationId;
        }
    }

    private String normalize(String headerName) {
        return headerName.toLowerCase(Locale.ROOT);
    }
}
