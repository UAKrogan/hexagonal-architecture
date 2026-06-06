package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.contract.model.ProblemDto;
import com.example.hexagonal.contract.model.ValidationErrorDto;
import com.example.hexagonal.contract.model.ValidationProblemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProblemResponseMapper {

    @Mapping(target = "type", expression = "java(\"urn:problem:\" + system + \":\" + type)")
    @Mapping(target = "system", source = "system")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "details", source = "details")
    ProblemDto toProblem(String system, String type, String title, String details);

    @Mapping(target = "problem", source = "problem")
    @Mapping(target = "validationErrors", source = "validationErrors")
    ValidationProblemDto toValidationProblem(ProblemDto problem, List<ValidationErrorDto> validationErrors);

    @Mapping(target = "field", source = "field")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "message", source = "message")
    ValidationErrorDto toValidationError(String field, String code, String message);
}
