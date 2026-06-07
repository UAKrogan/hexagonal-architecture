package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.application.error.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;
import java.util.Map;

final class HttpErrorDefinitions {

    private static final String SYSTEM = "hexagonal";

    private static final Map<ErrorCode, HttpErrorDefinition> DEFINITIONS = definitions();

    private HttpErrorDefinitions() {
    }

    static HttpErrorDefinition definition(ErrorCode errorCode) {
        HttpErrorDefinition definition = DEFINITIONS.get(errorCode);
        if (definition == null) {
            throw new IllegalArgumentException("No error definition configured for " + errorCode);
        }
        return definition;
    }

    private static Map<ErrorCode, HttpErrorDefinition> definitions() {
        Map<ErrorCode, HttpErrorDefinition> definitions = new EnumMap<>(ErrorCode.class);

        definitions.put(ErrorCode.MISSING_HEADER, validation(
            ErrorCode.MISSING_HEADER,
            "missing-header",
            "Missing required header"
        ));
        definitions.put(ErrorCode.VALIDATION_ERROR, validation(
            ErrorCode.VALIDATION_ERROR,
            "validation-error",
            "Validation failed"
        ));
        definitions.put(ErrorCode.MISSING_REQUEST_VALUE, validation(
            ErrorCode.MISSING_REQUEST_VALUE,
            "missing-request-value",
            "Missing required request value"
        ));
        definitions.put(ErrorCode.INVALID_API_VERSION, validation(
            ErrorCode.INVALID_API_VERSION,
            "invalid-api-version",
            "Invalid API version"
        ));
        definitions.put(ErrorCode.INVALID_JSON, validation(
            ErrorCode.INVALID_JSON,
            "invalid-json",
            "Invalid JSON"
        ));
        definitions.put(ErrorCode.INVALID_REQUEST_BODY, validation(
            ErrorCode.INVALID_REQUEST_BODY,
            "invalid-request-body",
            "Invalid request body"
        ));
        definitions.put(ErrorCode.BUSINESS_ERROR, validation(
            ErrorCode.BUSINESS_ERROR,
            "business-error",
            "Business rule violation"
        ));
        definitions.put(ErrorCode.APPLICATION_ERROR, validation(
            ErrorCode.APPLICATION_ERROR,
            "application-error",
            "Application error"
        ));
        definitions.put(ErrorCode.DOMAIN_ERROR, validation(
            ErrorCode.DOMAIN_ERROR,
            "domain-error",
            "Domain error"
        ));

        definitions.put(ErrorCode.UNSUPPORTED_MEDIA_TYPE, problem(
            ErrorCode.UNSUPPORTED_MEDIA_TYPE,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "unsupported-media-type",
            "Unsupported media type",
            true
        ));
        definitions.put(ErrorCode.METHOD_NOT_ALLOWED, problem(
            ErrorCode.METHOD_NOT_ALLOWED,
            HttpStatus.METHOD_NOT_ALLOWED,
            "method-not-allowed",
            "Method not allowed",
            true
        ));
        definitions.put(ErrorCode.CONTENT_NOT_FOUND, problem(
            ErrorCode.CONTENT_NOT_FOUND,
            HttpStatus.NOT_FOUND,
            "content-not-found",
            "Content not found",
            true
        ));
        definitions.put(ErrorCode.PROVIDER_UNAVAILABLE, problem(
            ErrorCode.PROVIDER_UNAVAILABLE,
            HttpStatus.BAD_GATEWAY,
            "provider-unavailable",
            "Content provider unavailable",
            true
        ));
        definitions.put(ErrorCode.ROUTE_NOT_FOUND, problem(
            ErrorCode.ROUTE_NOT_FOUND,
            HttpStatus.NOT_FOUND,
            "route-not-found",
            "Route not found",
            true
        ));
        definitions.put(ErrorCode.UNEXPECTED_ERROR, problem(
            ErrorCode.UNEXPECTED_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "unexpected-error",
            "Unexpected error",
            false
        ));

        return Map.copyOf(definitions);
    }

    private static HttpErrorDefinition validation(ErrorCode code, String type, String title) {
        return new DefaultHttpErrorDefinition(
            code,
            HttpStatus.BAD_REQUEST,
            SYSTEM,
            type,
            title,
            ErrorResponseBody.VALIDATION_PROBLEM,
            true
        );
    }

    private static HttpErrorDefinition problem(ErrorCode code,
                                           HttpStatus status,
                                           String type,
                                           String title,
                                           boolean expected) {

        return new DefaultHttpErrorDefinition(
            code,
            status,
            SYSTEM,
            type,
            title,
            ErrorResponseBody.PROBLEM,
            expected
        );
    }
}
