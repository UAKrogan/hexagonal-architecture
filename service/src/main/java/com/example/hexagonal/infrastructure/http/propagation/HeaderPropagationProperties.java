package com.example.hexagonal.infrastructure.http.propagation;

import com.example.hexagonal.infrastructure.context.RequestContextHeaders;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@ConfigurationProperties(prefix = "application.http.propagation")
public class HeaderPropagationProperties {

    @NotEmpty
    private final List<@NotBlank String> headers;

    private final Set<String> normalizedHeaders;

    public HeaderPropagationProperties(List<String> headers) {
        this.headers = headers == null ? List.of() : List.copyOf(headers);
        this.normalizedHeaders = this.headers.stream()
            .map(HeaderPropagationProperties::normalize)
            .collect(Collectors.toUnmodifiableSet());
    }

    public List<String> getHeaders() {
        return headers;
    }

    public Set<String> normalizedHeaders() {
        return normalizedHeaders;
    }

    @AssertTrue(message = "headers must include x-correlation-id and x-api-version")
    public boolean hasRequiredHeaders() {
        return normalizedHeaders.contains(RequestContextHeaders.CORRELATION_ID)
            && normalizedHeaders.contains(RequestContextHeaders.API_VERSION);
    }

    public boolean shouldPropagate(String headerName) {
        return normalizedHeaders.contains(normalize(headerName));
    }

    public static String normalize(String headerName) {
        return headerName.toLowerCase(Locale.ROOT);
    }
}
