package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.contract.model.ProblemDto;
import com.example.hexagonal.contract.model.ValidationErrorDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemResponseMapperTest {

    private final ProblemResponseMapper problemResponseMapper = Mappers.getMapper(ProblemResponseMapper.class);

    @Test
    void shouldMapProblemResponse() {
        ProblemDto problem = problemResponseMapper.toProblem(
            "hexagonal",
            "business-error",
            "Business rule violation",
            "The request violates a business rule."
        );

        assertThat(problem.getType()).isEqualTo("urn:problem:hexagonal:business-error");
        assertThat(problem.getSystem()).isEqualTo("hexagonal");
        assertThat(problem.getTitle()).isEqualTo("Business rule violation");
        assertThat(problem.getDetails()).isEqualTo("The request violates a business rule.");
    }

    @Test
    void shouldMapValidationProblemResponse() {
        ProblemDto problem = problemResponseMapper.toProblem("hexagonal", "validation-error", "Validation failed", "Invalid");
        ValidationErrorDto validationError = problemResponseMapper.toValidationError("contentId", "required", "Required");

        var validationProblem = problemResponseMapper.toValidationProblem(problem, List.of(validationError));

        assertThat(validationProblem.getProblem()).isEqualTo(problem);
        assertThat(validationProblem.getValidationErrors()).containsExactly(validationError);
    }

    @Test
    void shouldMapValidationError() {
        ValidationErrorDto validationError = problemResponseMapper.toValidationError("provider", "required", "Required");

        assertThat(validationError.getField()).isEqualTo("provider");
        assertThat(validationError.getCode()).isEqualTo("required");
        assertThat(validationError.getMessage()).isEqualTo("Required");
    }
}
