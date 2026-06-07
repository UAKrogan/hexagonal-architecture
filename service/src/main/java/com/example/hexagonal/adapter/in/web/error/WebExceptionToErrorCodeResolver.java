package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.application.error.ErrorCode;
import com.example.hexagonal.application.error.ExceptionToErrorCodeResolver;
import com.example.hexagonal.contract.api.ContentApi;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

@Component
class WebExceptionToErrorCodeResolver {

    private static final String API_VERSION_HEADER = "x-api-version";

    private final ExceptionToErrorCodeResolver exceptionToErrorCodeResolver =
        new ExceptionToErrorCodeResolver();

    ErrorCode resolve(Throwable error, ServerRequest request) {
        if (isMissingApiVersionHeader(error, request)) {
            return ErrorCode.MISSING_HEADER;
        }

        if (error instanceof WebExchangeBindException || error instanceof HandlerMethodValidationException) {
            return ErrorCode.VALIDATION_ERROR;
        }

        if (error instanceof MissingRequestValueException ex && isMissingHeader(ex)) {
            return ErrorCode.MISSING_HEADER;
        }

        if (error instanceof MissingRequestValueException) {
            return ErrorCode.MISSING_REQUEST_VALUE;
        }

        if (error instanceof InvalidApiVersionException) {
            return ErrorCode.INVALID_API_VERSION;
        }

        if (error instanceof UnsupportedMediaTypeStatusException) {
            return ErrorCode.UNSUPPORTED_MEDIA_TYPE;
        }

        if (error instanceof MethodNotAllowedException) {
            return ErrorCode.METHOD_NOT_ALLOWED;
        }

        if (error instanceof ServerWebInputException ex) {
            return hasCause(ex, DecodingException.class) || hasJsonCause(ex)
                ? ErrorCode.INVALID_JSON
                : ErrorCode.INVALID_REQUEST_BODY;
        }

        if (error instanceof ResponseStatusException ex && ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
            return ErrorCode.ROUTE_NOT_FOUND;
        }

        return exceptionToErrorCodeResolver.resolve(error)
            .orElse(ErrorCode.UNEXPECTED_ERROR);
    }

    private boolean isMissingApiVersionHeader(Throwable error, ServerRequest request) {
        return error instanceof ResponseStatusException ex
            && ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()
            && ContentApi.PATH_GET_CONTENT.equals(request.path())
            && request.headers().firstHeader(API_VERSION_HEADER) == null;
    }

    private boolean isMissingHeader(MissingRequestValueException ex) {
        String label = ex.getLabel();
        return label != null && label.toLowerCase().contains("header");
    }

    private boolean hasCause(Throwable error, Class<? extends Throwable> expectedType) {
        Throwable current = error;
        while (current != null) {
            if (expectedType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean hasJsonCause(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String simpleName = current.getClass().getSimpleName();
            if (simpleName.contains("Json") || simpleName.contains("JSON")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
