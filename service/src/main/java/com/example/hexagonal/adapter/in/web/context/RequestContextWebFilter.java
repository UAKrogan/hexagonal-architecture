package com.example.hexagonal.adapter.in.web.context;

import com.example.hexagonal.infrastructure.context.RequestContext;
import com.example.hexagonal.infrastructure.context.RequestContextFactory;
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
import java.util.List;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestContextWebFilter implements WebFilter {

    private final HeaderPropagationProperties headerPropagationProperties;
    private final RequestContextFactory requestContextFactory;

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
        return requestContextFactory.create(
            firstValueHeaders(exchange.getRequest().getHeaders()),
            headerPropagationProperties.normalizedHeaders(),
            exchange.getRequest().getId()
        );
    }

    private Map<String, String> firstValueHeaders(HttpHeaders httpHeaders) {
        Map<String, String> headers = new HashMap<>();

        httpHeaders.forEach((name, values) -> {
            if (!values.isEmpty()) {
                headers.put(name, firstValue(values));
            }
        });

        return Map.copyOf(headers);
    }

    private String firstValue(List<String> values) {
        return values.isEmpty() ? null : values.getFirst();
    }
}
