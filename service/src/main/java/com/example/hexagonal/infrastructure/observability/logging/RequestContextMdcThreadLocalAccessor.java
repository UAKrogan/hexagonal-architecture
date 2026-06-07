package com.example.hexagonal.infrastructure.observability.logging;

import com.example.hexagonal.infrastructure.observability.context.RequestContext;
import com.example.hexagonal.infrastructure.observability.context.RequestContextHeaders;
import io.micrometer.context.ThreadLocalAccessor;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class RequestContextMdcThreadLocalAccessor implements ThreadLocalAccessor<Map<String, String>> {

    public static final String KEY = "request-context-mdc";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public Map<String, String> getValue() {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return contextMap == null ? Map.of() : contextMap;
    }

    @Override
    public void setValue(Map<String, String> value) {
        if (value == null || value.isEmpty()) {
            MDC.clear();
            return;
        }

        MDC.setContextMap(value);
    }

    @Override
    public void setValue() {
        MDC.clear();
    }

    @Override
    public void restore() {
        MDC.clear();
    }

    public static Map<String, String> toMdcContext(RequestContext requestContext) {
        Map<String, String> mdcContext = new HashMap<>();
        mdcContext.put(RequestContextHeaders.CORRELATION_ID, requestContext.correlationId());
        mdcContext.put(RequestContextHeaders.API_VERSION, requestContext.apiVersion());
        return Map.copyOf(mdcContext);
    }
}
