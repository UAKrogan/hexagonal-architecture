package com.example.hexagonal.adapter.in.web.error;

import com.example.hexagonal.application.error.ErrorCode;
import com.example.hexagonal.contract.model.ProblemDto;
import com.example.hexagonal.contract.model.ValidationErrorDto;
import com.example.hexagonal.contract.model.ValidationProblemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProblemResponseMapper {

    @Mapping(target = "type", expression = "java(problemType(errorCode))")
    @Mapping(target = "system", expression = "java(definition(errorCode).system())")
    @Mapping(target = "title", expression = "java(definition(errorCode).title())")
    @Mapping(target = "details", source = "details")
    ProblemDto toProblem(ErrorCode errorCode, String details);

    @Mapping(target = "problem", source = "problem")
    @Mapping(target = "validationErrors", source = "validationErrors")
    ValidationProblemDto toValidationProblem(ProblemDto problem, List<ValidationErrorDto> validationErrors);

    @Mapping(target = "field", source = "field")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "message", source = "message")
    ValidationErrorDto toValidationError(String field, String code, String message);

    default HttpErrorDefinition definition(ErrorCode errorCode) {
        return HttpErrorDefinitions.definition(errorCode);
    }

    default String problemType(ErrorCode errorCode) {
        HttpErrorDefinition definition = definition(errorCode);
        return "urn:problem:" + definition.system() + ":" + definition.type();
    }
}
