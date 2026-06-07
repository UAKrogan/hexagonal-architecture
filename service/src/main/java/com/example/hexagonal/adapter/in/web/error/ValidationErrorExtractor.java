package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.application.error.ErrorCode;
import com.example.hexagonal.contract.model.ValidationErrorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ServerWebInputException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
class ValidationErrorExtractor {

    private static final String API_VERSION_HEADER = "x-api-version";

    private final ProblemResponseMapper problemResponseMapper;

    List<ValidationErrorDto> extract(ErrorCode errorCode, Throwable error) {
        return switch (errorCode) {
            case MISSING_HEADER -> List.of(missingHeaderValidationError(error));
            case VALIDATION_ERROR -> validationErrors(error);
            case MISSING_REQUEST_VALUE -> List.of(missingRequestValueValidationError(error));
            case INVALID_API_VERSION -> List.of(
                validationError(API_VERSION_HEADER, "invalid", "Requested API version is not supported.")
            );
            case INVALID_JSON, INVALID_REQUEST_BODY -> List.of(requestBodyValidationError(errorCode, error));
            case BUSINESS_ERROR, APPLICATION_ERROR, DOMAIN_ERROR -> List.of();
            default -> List.of();
        };
    }

    private List<ValidationErrorDto> validationErrors(Throwable error) {
        if (error instanceof WebExchangeBindException ex) {
            return validationErrors(ex);
        }
        if (error instanceof HandlerMethodValidationException ex) {
            return validationErrors(ex);
        }
        return List.of();
    }

    private ValidationErrorDto missingHeaderValidationError(Throwable error) {
        if (error instanceof MissingRequestValueException ex) {
            String label = requestValueLabel(ex);
            return validationError(ex.getName(), "required", "Required " + label + " is missing.");
        }

        return validationError(API_VERSION_HEADER, "required", "Required request header is missing.");
    }

    private ValidationErrorDto missingRequestValueValidationError(Throwable error) {
        if (error instanceof MissingRequestValueException ex) {
            String label = requestValueLabel(ex);
            return validationError(ex.getName(), "required", "Required " + label + " is missing.");
        }

        return validationError("request", "required", "Required request value is missing.");
    }

    private ValidationErrorDto requestBodyValidationError(ErrorCode errorCode, Throwable error) {
        String code = errorCode == ErrorCode.INVALID_JSON ? "invalidJson" : "invalidBody";

        if (error instanceof ServerWebInputException ex) {
            return validationError("body", code, nonBlankOrDefault(ex.getReason(), "Invalid request body."));
        }

        return validationError("body", code, "Invalid request body.");
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

    private String requestValueLabel(MissingRequestValueException ex) {
        return Objects.requireNonNullElse(ex.getLabel(), "request value");
    }

    private String nonBlankOrDefault(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
