package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.application.error.ErrorCode;
import com.example.hexagonal.contract.model.ValidationErrorDto;
import com.example.hexagonal.contract.model.ValidationProblemDto;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ErrorResponseResolver {

    private final ProblemResponseMapper problemResponseMapper;
    private final WebExceptionToErrorCodeResolver exceptionToErrorCodeResolver;
    private final ValidationErrorExtractor validationErrorExtractor;

    ErrorResponse toErrorResponse(Throwable error, ServerRequest request) {
        ErrorCode errorCode = exceptionToErrorCodeResolver.resolve(error, request);
        HttpErrorDefinition definition = HttpErrorDefinitions.definition(errorCode);

        Object body = switch (definition.responseBody()) {
            case PROBLEM -> problem(errorCode, details(errorCode, error, request));
            case VALIDATION_PROBLEM -> validationProblem(
                errorCode,
                details(errorCode, error, request),
                validationErrorExtractor.extract(errorCode, error)
            );
        };

        return new ErrorResponse(definition.status(), body, definition.expected());
    }

    private Object problem(ErrorCode errorCode, String details) {
        return problemResponseMapper.toProblem(errorCode, details);
    }

    private Object validationProblem(ErrorCode errorCode,
                                     String details,
                                     List<ValidationErrorDto> validationErrors) {

        ValidationProblemDto body = problemResponseMapper.toValidationProblem(
            problemResponseMapper.toProblem(errorCode, details),
            validationErrors
        );

        return body;
    }

    private String details(ErrorCode errorCode, Throwable error, ServerRequest request) {
        return switch (errorCode) {
            case MISSING_HEADER -> missingHeaderDetails(error);
            case VALIDATION_ERROR -> "Request validation failed.";
            case MISSING_REQUEST_VALUE -> missingRequestValueDetails(error);
            case INVALID_API_VERSION -> invalidApiVersionDetails(error);
            case UNSUPPORTED_MEDIA_TYPE -> unsupportedMediaTypeDetails(error);
            case METHOD_NOT_ALLOWED -> methodNotAllowedDetails(error, request);
            case INVALID_JSON, INVALID_REQUEST_BODY -> requestBodyDetails(error);
            case CONTENT_NOT_FOUND -> nonBlankOrDefault(error.getMessage(), "The requested content could not be found.");
            case PROVIDER_UNAVAILABLE -> nonBlankOrDefault(error.getMessage(), "The content provider is unavailable.");
            case BUSINESS_ERROR -> nonBlankOrDefault(error.getMessage(), "The request violates a business rule.");
            case APPLICATION_ERROR -> nonBlankOrDefault(error.getMessage(), "The request could not be processed.");
            case DOMAIN_ERROR -> nonBlankOrDefault(error.getMessage(), "The request violates a domain rule.");
            case ROUTE_NOT_FOUND -> "No route found for " + request.method().name() + " " + request.path() + ".";
            case UNEXPECTED_ERROR -> "An unexpected error occurred while processing the request.";
        };
    }

    private String missingHeaderDetails(Throwable error) {
        if (error instanceof MissingRequestValueException ex) {
            String label = requestValueLabel(ex);
            return "Required " + label + " '" + ex.getName() + "' is missing.";
        }

        return "Required request header 'x-api-version' is missing.";
    }

    private String missingRequestValueDetails(Throwable error) {
        if (error instanceof MissingRequestValueException ex) {
            String label = requestValueLabel(ex);
            return "Required " + label + " '" + ex.getName() + "' is missing.";
        }

        return "Required request value is missing.";
    }

    private String requestValueLabel(MissingRequestValueException ex) {
        return Objects.requireNonNullElse(ex.getLabel(), "request value");
    }

    private String invalidApiVersionDetails(Throwable error) {
        if (error instanceof InvalidApiVersionException ex) {
            return nonBlankOrDefault(ex.getReason(), "The requested API version is not supported.");
        }

        return "The requested API version is not supported.";
    }

    private String unsupportedMediaTypeDetails(Throwable error) {
        if (error instanceof UnsupportedMediaTypeStatusException ex) {
            return nonBlankOrDefault(ex.getReason(), "The request media type is not supported.");
        }

        return "The request media type is not supported.";
    }

    private String methodNotAllowedDetails(Throwable error, ServerRequest request) {
        if (error instanceof MethodNotAllowedException ex) {
            return "HTTP method " + ex.getHttpMethod() + " is not supported for " + request.path() + ".";
        }

        return "HTTP method " + request.method().name() + " is not supported for " + request.path() + ".";
    }

    private String requestBodyDetails(Throwable error) {
        if (error instanceof ServerWebInputException ex) {
            return nonBlankOrDefault(ex.getReason(), "The request body is invalid.");
        }

        return "The request body is invalid.";
    }

    private String nonBlankOrDefault(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
