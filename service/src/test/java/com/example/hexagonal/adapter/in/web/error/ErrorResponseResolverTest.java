package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.application.error.ExceptionToErrorCodeResolver;
import com.example.hexagonal.application.exception.ApplicationException;
import com.example.hexagonal.application.exception.ProviderUnavailableException;
import com.example.hexagonal.contract.model.ProblemDto;
import com.example.hexagonal.contract.model.ValidationProblemDto;
import com.example.hexagonal.domain.exception.BusinessException;
import com.example.hexagonal.domain.exception.ContentNotFoundException;
import com.example.hexagonal.domain.exception.DomainException;
import com.example.hexagonal.test.tag.UnitTest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class ErrorResponseResolverTest {

    private final ErrorResponseResolver resolver =
        new ErrorResponseResolver(
            Mappers.getMapper(ProblemResponseMapper.class),
            new WebExceptionToErrorCodeResolver(new ExceptionToErrorCodeResolver()),
            new ValidationErrorExtractor(Mappers.getMapper(ProblemResponseMapper.class))
        );

    @Test
    void shouldMapBusinessExceptionToBadRequestValidationProblem() {
        ErrorResponse response =
            resolver.toErrorResponse(new BusinessException("business failed"), request("/api/content"));

        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.expected()).isTrue();
        assertValidationProblem(response.body(), "urn:problem:hexagonal:business-error", "business failed");
    }

    @Test
    void shouldMapApplicationExceptionToBadRequestValidationProblem() {
        ErrorResponse response =
            resolver.toErrorResponse(new ApplicationException("application failed"), request("/api/content"));

        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertValidationProblem(response.body(), "urn:problem:hexagonal:application-error", "application failed");
    }

    @Test
    void shouldMapDomainExceptionToBadRequestValidationProblem() {
        ErrorResponse response = resolver.toErrorResponse(new DomainException("domain failed"), request("/api/content"));

        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertValidationProblem(response.body(), "urn:problem:hexagonal:domain-error", "domain failed");
    }

    @Test
    void shouldMapContentNotFoundExceptionToNotFoundProblem() {
        ErrorResponse response =
            resolver.toErrorResponse(new ContentNotFoundException("content missing"), request("/api/content"));

        assertThat(response.status()).isEqualTo(HttpStatus.NOT_FOUND);
        assertProblem(response.body(), "urn:problem:hexagonal:content-not-found", "content missing");
    }

    @Test
    void shouldMapProviderUnavailableExceptionToBadGatewayProblem() {
        ErrorResponse response = resolver.toErrorResponse(
            new ProviderUnavailableException("provider down"),
            request("/api/content")
        );

        assertThat(response.status()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertProblem(response.body(), "urn:problem:hexagonal:provider-unavailable", "provider down");
    }

    @Test
    void shouldMapMethodNotAllowedExceptionToMethodNotAllowedProblem() {
        ErrorResponse response = resolver.toErrorResponse(
            new MethodNotAllowedException(HttpMethod.PUT, List.of(HttpMethod.POST)),
            request("/api/content", HttpMethod.PUT)
        );

        assertThat(response.status()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertProblem(response.body(), "urn:problem:hexagonal:method-not-allowed", "HTTP method PUT is not supported");
    }

    @Test
    void shouldMapUnsupportedMediaTypeExceptionToUnsupportedMediaTypeProblem() {
        ErrorResponse response = resolver.toErrorResponse(
            new UnsupportedMediaTypeStatusException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)),
            request("/api/content")
        );

        assertThat(response.status()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertProblem(response.body(), "urn:problem:hexagonal:unsupported-media-type", "text/plain");
    }

    @Test
    void shouldMapMissingApiVersionToBadRequestValidationProblem() {
        ErrorResponse response = resolver.toErrorResponse(
            new ResponseStatusException(HttpStatus.NOT_FOUND),
            request("/api/content")
        );

        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertValidationProblem(response.body(), "urn:problem:hexagonal:missing-header", "x-api-version");
    }

    @Test
    void shouldMapRouteNotFoundToNotFoundProblem() {
        ErrorResponse response = resolver.toErrorResponse(
            new ResponseStatusException(HttpStatus.NOT_FOUND),
            request("/api/unknown", HttpMethod.GET, "x-api-version", "1")
        );

        assertThat(response.status()).isEqualTo(HttpStatus.NOT_FOUND);
        assertProblem(response.body(), "urn:problem:hexagonal:route-not-found", "No route found");
    }

    @Test
    void shouldMapUnexpectedExceptionToInternalServerErrorProblem() {
        ErrorResponse response = resolver.toErrorResponse(new IllegalStateException("boom"), request("/api/content"));

        assertThat(response.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.expected()).isFalse();
        assertProblem(response.body(), "urn:problem:hexagonal:unexpected-error", "unexpected error");
    }

    private ServerRequest request(String path) {
        return request(path, HttpMethod.POST);
    }

    private ServerRequest request(String path, HttpMethod method) {
        return request(path, method, null, null);
    }

    private ServerRequest request(String path, HttpMethod method, String headerName, String headerValue) {
        MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest.method(method, path);
        if (headerName != null) {
            requestBuilder.header(headerName, headerValue);
        }

        List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
        return ServerRequest.create(MockServerWebExchange.from(requestBuilder), messageReaders);
    }

    private void assertProblem(Object body, String type, String detailsSubstring) {
        assertThat(body).isInstanceOf(ProblemDto.class);

        ProblemDto problem = (ProblemDto) body;

        assertThat(problem.getType()).isEqualTo(type);
        assertThat(problem.getSystem()).isEqualTo("hexagonal");
        assertThat(problem.getDetails()).containsIgnoringCase(detailsSubstring);
    }

    private void assertValidationProblem(Object body, String type, String detailsSubstring) {
        assertThat(body).isInstanceOf(ValidationProblemDto.class);

        ValidationProblemDto validationProblem = (ValidationProblemDto) body;

        assertThat(validationProblem.getProblem()).isNotNull();
        assertThat(validationProblem.getProblem().getType()).isEqualTo(type);
        assertThat(validationProblem.getProblem().getDetails()).containsIgnoringCase(detailsSubstring);
    }
}
