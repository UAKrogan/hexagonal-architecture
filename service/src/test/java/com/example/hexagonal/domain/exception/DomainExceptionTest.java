package com.example.hexagonal.domain.exception;

import com.example.hexagonal.test.tag.UnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class DomainExceptionTest {

    @Test
    void shouldPreserveDomainExceptionMessageAndCause() {
        RuntimeException cause = new RuntimeException("cause");

        DomainException exception = new DomainException("domain failed", cause);

        assertThat(exception)
            .hasMessage("domain failed")
            .hasCause(cause);
    }

    @Test
    void businessExceptionShouldBeDomainException() {
        BusinessException exception = new BusinessException("business failed");

        assertThat(exception)
            .isInstanceOf(DomainException.class)
            .hasMessage("business failed");
    }

    @Test
    void contentNotFoundExceptionShouldBeDomainException() {
        ContentNotFoundException exception = new ContentNotFoundException("content missing");

        assertThat(exception)
            .isInstanceOf(DomainException.class)
            .hasMessage("content missing");
    }
}
