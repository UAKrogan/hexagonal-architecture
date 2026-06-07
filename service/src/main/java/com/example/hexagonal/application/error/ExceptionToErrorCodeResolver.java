package com.example.hexagonal.application.error;

import com.example.hexagonal.application.exception.ApplicationException;
import com.example.hexagonal.application.exception.ProviderUnavailableException;
import com.example.hexagonal.domain.exception.BusinessException;
import com.example.hexagonal.domain.exception.ContentNotFoundException;
import com.example.hexagonal.domain.exception.DomainException;

import java.util.Optional;

public class ExceptionToErrorCodeResolver {

    public Optional<ErrorCode> resolve(Throwable error) {
        if (error instanceof ContentNotFoundException) {
            return Optional.of(ErrorCode.CONTENT_NOT_FOUND);
        }

        if (error instanceof ProviderUnavailableException) {
            return Optional.of(ErrorCode.PROVIDER_UNAVAILABLE);
        }

        if (error instanceof BusinessException) {
            return Optional.of(ErrorCode.BUSINESS_ERROR);
        }

        if (error instanceof ApplicationException) {
            return Optional.of(ErrorCode.APPLICATION_ERROR);
        }

        if (error instanceof DomainException) {
            return Optional.of(ErrorCode.DOMAIN_ERROR);
        }

        return Optional.empty();
    }
}
