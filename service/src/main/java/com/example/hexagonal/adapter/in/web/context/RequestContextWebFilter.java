package com.example.hexagonal.adapter.in.web.context;

import com.example.hexagonal.infrastructure.context.RequestContext;
import com.example.hexagonal.infrastructure.context.RequestContextHeaders;
import com.example.hexagonal.infrastructure.http.propagation.HeaderPropagationProperties;
import com.example.hexagonal.infrastructure.logging.RequestContextMdcThreadLocalAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestContextWebFilter implements WebFilter {

    private static final String UNKNOWN = "unknown";

    private final HeaderPropagationProperties headerPropagationProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        RequestContext requestContext = createRequestContext(exchange);
        Map<String, String> mdcContext = RequestContextMdcThreadLocalAccessor.toMdcContext(requestContext);

        return chain.filter(exchange)
            .contextWrite(context -> context
                .put(RequestContext.class, requestContext)
                .put(RequestContextMdcThreadLocalAccessor.KEY, mdcContext)
            );
    }

    private RequestContext createRequestContext(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String correlationId = firstHeaderOrDefault(
            headers,
            RequestContextHeaders.CORRELATION_ID,
            exchange.getRequest().getId()
        );
        String apiVersion = firstHeaderOrDefault(headers, RequestContextHeaders.API_VERSION, UNKNOWN);

        Map<String, String> propagatedHeaders = new HashMap<>();
        for (String headerName : headerPropagationProperties.normalizedHeaders()) {
            String value = headers.getFirst(headerName);
            if (value != null) {
                propagatedHeaders.put(headerName, value);
            }
        }

        propagatedHeaders.putIfAbsent(RequestContextHeaders.CORRELATION_ID, correlationId);
        propagatedHeaders.putIfAbsent(RequestContextHeaders.API_VERSION, apiVersion);

        return new RequestContext(correlationId, apiVersion, propagatedHeaders);
    }

    private String firstHeaderOrDefault(HttpHeaders headers, String headerName, String defaultValue) {
        String value = headers.getFirst(headerName);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
