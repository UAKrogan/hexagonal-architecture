package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.application.exception.ApplicationException;
import com.example.hexagonal.application.exception.ProviderUnavailableException;
import com.example.hexagonal.contract.api.ContentApi;
import com.example.hexagonal.contract.model.ValidationErrorDto;
import com.example.hexagonal.contract.model.ValidationProblemDto;
import com.example.hexagonal.domain.exception.BusinessException;
import com.example.hexagonal.domain.exception.ContentNotFoundException;
import com.example.hexagonal.domain.exception.DomainException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ErrorResponseResolver {

    private static final String SYSTEM = "hexagonal";
    private static final String API_VERSION_HEADER = "x-api-version";

    private final ProblemResponseMapper problemResponseMapper;

    ErrorResponse toErrorResponse(Throwable error, ServerRequest request) {

        if (isMissingApiVersionHeader(error, request)) {
            return validationProblem(
                "missing-header",
                "Missing required header",
                "Required request header 'x-api-version' is missing.",
                List.of(validationError(API_VERSION_HEADER, "required", "Required request header is missing."))
            );
        }

        if (error instanceof WebExchangeBindException ex) {
            return validationProblem(
                "validation-error",
                "Validation failed",
                "Request validation failed.",
                validationErrors(ex)
            );
        }

        if (error instanceof HandlerMethodValidationException ex) {
            return validationProblem(
                "validation-error",
                "Validation failed",
                "Request validation failed.",
                validationErrors(ex)
            );
        }

        if (error instanceof MissingRequestValueException ex) {
            String label = Objects.requireNonNullElse(ex.getLabel(), "request value");
            return validationProblem(
                "missing-" + label.replace(' ', '-').toLowerCase(Locale.ROOT),
                "Missing required " + label,
                "Required " + label + " '" + ex.getName() + "' is missing.",
                List.of(validationError(ex.getName(), "required", "Required " + label + " is missing."))
            );
        }

        if (error instanceof UnsupportedMediaTypeStatusException ex) {
            return problem(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "unsupported-media-type",
                "Unsupported media type",
                nonBlankOrDefault(ex.getReason(), "The request media type is not supported.")
            );
        }

        if (error instanceof MethodNotAllowedException ex) {
            return problem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "method-not-allowed",
                "Method not allowed",
                "HTTP method " + ex.getHttpMethod() + " is not supported for " + request.path() + "."
            );
        }

        if (error instanceof ServerWebInputException ex) {
            return validationProblem(
                inputProblemType(ex),
                inputProblemTitle(ex),
                nonBlankOrDefault(ex.getReason(), "The request body is invalid."),
                List.of(validationError("body", inputProblemCode(ex), nonBlankOrDefault(ex.getReason(), "Invalid request body.")))
            );
        }

        if (error instanceof ContentNotFoundException ex) {
            return problem(
                HttpStatus.NOT_FOUND,
                "content-not-found",
                "Content not found",
                nonBlankOrDefault(ex.getMessage(), "The requested content could not be found.")
            );
        }

        if (error instanceof ProviderUnavailableException ex) {
            return problem(
                HttpStatus.BAD_GATEWAY,
                "provider-unavailable",
                "Content provider unavailable",
                nonBlankOrDefault(ex.getMessage(), "The content provider is unavailable.")
            );
        }

        if (error instanceof BusinessException ex) {
            return validationProblem(
                "business-error",
                "Business rule violation",
                nonBlankOrDefault(ex.getMessage(), "The request violates a business rule."),
                List.of()
            );
        }

        if (error instanceof ApplicationException ex) {
            return validationProblem(
                "application-error",
                "Application error",
                nonBlankOrDefault(ex.getMessage(), "The request could not be processed."),
                List.of()
            );
        }

        if (error instanceof DomainException ex) {
            return validationProblem(
                "domain-error",
                "Domain error",
                nonBlankOrDefault(ex.getMessage(), "The request violates a domain rule."),
                List.of()
            );
        }

        if (error instanceof ResponseStatusException ex && ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
            return problem(
                HttpStatus.NOT_FOUND,
                "route-not-found",
                "Route not found",
                "No route found for " + request.method().name() + " " + request.path() + "."
            );
        }

        return problem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "unexpected-error",
            "Unexpected error",
            "An unexpected error occurred while processing the request.",
            false
        );
    }

    private boolean isMissingApiVersionHeader(Throwable error, ServerRequest request) {
        return error instanceof ResponseStatusException ex
            && ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()
            && ContentApi.PATH_GET_CONTENT.equals(request.path())
            && request.headers().firstHeader(API_VERSION_HEADER) == null;
    }

    private ErrorResponse problem(HttpStatus status, String type, String title, String details) {
        return problem(status, type, title, details, true);
    }

    private ErrorResponse problem(HttpStatus status, String type, String title, String details, boolean expected) {
        return new ErrorResponse(status, problemResponseMapper.toProblem(SYSTEM, type, title, details), expected);
    }

    private ErrorResponse validationProblem(String type,
                                            String title,
                                            String details,
                                            List<ValidationErrorDto> validationErrors) {

        ValidationProblemDto body = problemResponseMapper.toValidationProblem(
            problemResponseMapper.toProblem(SYSTEM, type, title, details),
            validationErrors
        );

        return new ErrorResponse(HttpStatus.BAD_REQUEST, body, true);
    }

    private List<ValidationErrorDto> validationErrors(WebExchangeBindException ex) {
        List<ValidationErrorDto> errors = new ArrayList<>();

        for (FieldError fieldError : ex.getFieldErrors()) {
            errors.add(validationError(
                fieldError.getField(),
                normalizeCode(fieldError.getCode()),
                nonBlankOrDefault(fieldError.getDefaultMessage(), "Invalid field value.")
            ));
        }

        for (ObjectError globalError : ex.getGlobalErrors()) {
            errors.add(validationError(
                globalError.getObjectName(),
                normalizeCode(globalError.getCode()),
                nonBlankOrDefault(globalError.getDefaultMessage(), "Invalid object value.")
            ));
        }

        return errors;
    }

    private List<ValidationErrorDto> validationErrors(HandlerMethodValidationException ex) {
        List<ValidationErrorDto> errors = new ArrayList<>();

        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                for (FieldError fieldError : parameterErrors.getFieldErrors()) {
                    errors.add(validationError(
                        fieldError.getField(),
                        normalizeCode(fieldError.getCode()),
                        nonBlankOrDefault(fieldError.getDefaultMessage(), "Invalid field value.")
                    ));
                }
            }

            for (MessageSourceResolvable resolvable : result.getResolvableErrors()) {
                errors.add(validationError(
                    parameterName(result.getMethodParameter()),
                    normalizeCode(firstCode(resolvable)),
                    nonBlankOrDefault(resolvable.getDefaultMessage(), "Invalid parameter value.")
                ));
            }
        }

        if (errors.isEmpty()) {
            errors.add(validationError("request", "invalid", "Request validation failed."));
        }

        return errors;
    }

    private String parameterName(MethodParameter parameter) {
        RequestHeader requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            return annotationName(requestHeader.name(), requestHeader.value(), parameter);
        }

        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            return annotationName(requestParam.name(), requestParam.value(), parameter);
        }

        String parameterName = parameter.getParameterName();
        return parameterName != null ? parameterName : "request";
    }

    private String annotationName(String name, String value, MethodParameter parameter) {
        if (hasText(name)) {
            return name;
        }
        if (hasText(value)) {
            return value;
        }
        String parameterName = parameter.getParameterName();
        return parameterName != null ? parameterName : "request";
    }

    private String inputProblemType(ServerWebInputException ex) {
        return hasCause(ex, DecodingException.class) || hasJsonCause(ex) ? "invalid-json" : "invalid-request-body";
    }

    private String inputProblemTitle(ServerWebInputException ex) {
        return hasCause(ex, DecodingException.class) || hasJsonCause(ex) ? "Invalid JSON" : "Invalid request body";
    }

    private String inputProblemCode(ServerWebInputException ex) {
        return hasCause(ex, DecodingException.class) || hasJsonCause(ex) ? "invalidJson" : "invalidBody";
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

    private ValidationErrorDto validationError(String field, String code, String message) {
        return problemResponseMapper.toValidationError(field, code, message);
    }

    private String firstCode(MessageSourceResolvable resolvable) {
        String[] codes = resolvable.getCodes();
        return codes != null && codes.length > 0 ? codes[0] : "invalid";
    }

    private String normalizeCode(String code) {
        if (!hasText(code)) {
            return "invalid";
        }

        String shortCode = code.contains(".") ? code.substring(0, code.indexOf('.')) : code;

        return switch (shortCode) {
            case "NotNull", "NotBlank", "NotEmpty" -> "required";
            case "typeMismatch" -> "invalid";
            default -> shortCode.substring(0, 1).toLowerCase(Locale.ROOT) + shortCode.substring(1);
        };
    }

    private String nonBlankOrDefault(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
