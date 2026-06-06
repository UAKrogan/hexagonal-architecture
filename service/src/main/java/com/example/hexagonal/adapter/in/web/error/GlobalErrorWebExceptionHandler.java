package com.example.hexagonal.adapter.in.web.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.autoconfigure.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    private final ErrorResponseResolver errorResponseResolver;

    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                          WebProperties webProperties,
                                          ApplicationContext applicationContext,
                                          ServerCodecConfigurer serverCodecConfigurer,
                                          ObjectProvider<ViewResolver> viewResolvers,
                                          ErrorResponseResolver errorResponseResolver) {

        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.errorResponseResolver = errorResponseResolver;
        setMessageWriters(serverCodecConfigurer.getWriters());
        setMessageReaders(serverCodecConfigurer.getReaders());
        setViewResolvers(viewResolvers.orderedStream().toList());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        ErrorResponse errorResponse = errorResponseResolver.toErrorResponse(error, request);

        log(request, error, errorResponse);

        return ServerResponse
            .status(errorResponse.status())
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .bodyValue(errorResponse.body());
    }

    private void log(ServerRequest request, Throwable error, ErrorResponse errorResponse) {
        if (errorResponse.expected()) {
            log.warn(
                "Handled request error: method={}, path={}, status={}, errorType={}, message={}",
                request.method().name(),
                request.path(),
                errorResponse.status().value(),
                error.getClass().getSimpleName(),
                error.getMessage()
            );
            return;
        }

        log.error(
            "Handled unexpected request error: method={}, path={}, status={}",
            request.method().name(),
            request.path(),
            errorResponse.status().value(),
            error
        );
    }
}
