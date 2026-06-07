package com.example.hexagonal;

import com.example.hexagonal.test.tag.ArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit tests that protect the hexagonal architecture boundaries.
 */
@AnalyzeClasses(
    packages = "com.example.hexagonal",
    importOptions = {
        ImportOption.DoNotIncludeTests.class
    }
)
@ArchitectureTest
public class HexagonalArchitectureTest {

    // ==============================================
    // Layer Dependency Rules
    // ==============================================

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_APPLICATION =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application..")
            .because("Domain must not know application use cases or orchestration concerns");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_ADAPTERS =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter..")
            .because("Domain must be isolated from inbound and outbound adapters");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_INFRASTRUCTURE =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .because("Domain must not depend on infrastructure implementation details");

    @ArchTest
    static final ArchRule APPLICATION_SHOULD_NOT_DEPEND_ON_ADAPTERS =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter..")
            .because("Application must depend on ports and domain, not adapter implementations");

    @ArchTest
    static final ArchRule APPLICATION_SHOULD_NOT_DEPEND_ON_INFRASTRUCTURE =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .because("Application orchestration must not depend on infrastructure implementation details");

    @ArchTest
    static final ArchRule ADAPTER_IN_SHOULD_NOT_DEPEND_ON_ADAPTER_OUT =
        noClasses()
            .that().resideInAPackage("..adapter.in..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter.out..")
            .because(
                "Inbound adapters must enter through application ports and must not call outbound adapters directly");

    @ArchTest
    static final ArchRule ADAPTER_OUT_SHOULD_NOT_DEPEND_ON_ADAPTER_IN =
        noClasses()
            .that().resideInAPackage("..adapter.out..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter.in..")
            .because("Outbound adapters must not depend on transport-specific inbound adapter concerns");

    @ArchTest
    static final ArchRule ADAPTER_OUT_SHOULD_NOT_DEPEND_ON_APPLICATION_SERVICES =
        noClasses()
            .that().resideInAPackage("..adapter.out..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application.service..")
            .because("Outbound adapters should depend on output ports, not application service internals");

    @ArchTest
    static final ArchRule INFRASTRUCTURE_SHOULD_NOT_DEPEND_ON_ADAPTER_IN =
        noClasses()
            .that().resideInAPackage("..infrastructure..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter.in..")
            .because("Infrastructure must remain reusable and must not depend on inbound transport implementations");

    @ArchTest
    static final ArchRule WEB_CONTROLLERS_SHOULD_NOT_DEPEND_ON_APPLICATION_SERVICES =
        noClasses()
            .that().resideInAPackage("..adapter.in.web..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat()
            .resideInAPackage("..application.service..")
            .because("Controllers should use input ports rather than concrete application services");

    @ArchTest
    static final ArchRule OUTPUT_ADAPTERS_SHOULD_DEPEND_ON_OUTPUT_PORTS =
        classes()
            .that().resideInAPackage("..adapter.out..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().dependOnClassesThat()
            .resideInAPackage("..application.port.out..")
            .because("Outbound adapters must be connected to the application through output ports");

    // ==============================================
    // Generated Contract Rules
    // ==============================================

    @ArchTest
    static final ArchRule GENERATED_CONTRACTS_SHOULD_NOT_BE_USED_FROM_DOMAIN =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..contract.api..", "..contract.model..")
            .because("Generated OpenAPI contracts are transport models and must not leak into the domain");

    @ArchTest
    static final ArchRule GENERATED_CONTRACTS_SHOULD_NOT_BE_USED_FROM_APPLICATION =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..contract.api..", "..contract.model..")
            .because("Generated OpenAPI contracts are adapter-facing DTOs and must not leak into application use cases");

    @ArchTest
    static final ArchRule GENERATED_CONTRACTS_SHOULD_NOT_BE_USED_FROM_ADAPTER_OUT =
        noClasses()
            .that().resideInAPackage("..adapter.out..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..contract.api..", "..contract.model..")
            .because("Generated OpenAPI contracts belong to inbound web adapters, not outbound integrations");

    @ArchTest
    static final ArchRule GENERATED_CONTRACTS_SHOULD_ONLY_BE_USED_BY_INBOUND_WEB_ADAPTERS =
        noClasses()
            .that().resideOutsideOfPackage("..adapter.in.web..")
            .and().resideOutsideOfPackage("..contract..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..contract.api..", "..contract.model..")
            .because("Generated OpenAPI contracts are HTTP adapter contracts and must not become shared models");

    @ArchTest
    static final ArchRule SHARED_ERROR_CLASSIFICATION_SHOULD_NOT_DEPEND_ON_WEB_OR_SPRING =
        noClasses()
            .that().resideInAPackage("..application.error..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..adapter..", "..infrastructure..", "org.springframework..", "reactor..")
            .because("Shared error codes and classification must be reusable by Web, Kafka, AMQP, and gRPC adapters");

    // ==============================================
    // Framework Isolation Rules
    // ==============================================

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_SPRING =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .because("Domain must be framework-agnostic");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_REACTOR =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("reactor..")
            .because("Domain must not expose reactive framework types");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_JSON_LIBRARIES =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.fasterxml.jackson..", "tools.jackson..")
            .because("JSON serialization is an adapter concern");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_MAPPING_FRAMEWORKS =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.mapstruct..")
            .because("Object mapping frameworks belong in adapters");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_VALIDATION_FRAMEWORKS =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("jakarta.validation..", "javax.validation..")
            .because("Domain should express validation with domain rules, not framework annotations");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_LOGGING_FRAMEWORKS =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.slf4j..", "ch.qos.logback..")
            .because("Domain decisions should be observable from adapters and infrastructure, not through logging APIs");

    @ArchTest
    static final ArchRule APPLICATION_SHOULD_NOT_DEPEND_ON_SPRING =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .because("Application use cases and ports should be wired by infrastructure, not Spring annotations");

    @ArchTest
    static final ArchRule APPLICATION_PORTS_SHOULD_NOT_DEPEND_ON_SPRING =
        noClasses()
            .that().resideInAPackage("..application.port..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .because("Application ports should remain adapter-neutral contracts");

    @ArchTest
    static final ArchRule APPLICATION_PORT_MODELS_SHOULD_NOT_DEPEND_ON_FRAMEWORK_DTOS =
        noClasses()
            .that().resideInAnyPackage("..application.port.in.command..", "..application.port.in.result..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.validation..",
                "javax.validation..",
                "com.fasterxml.jackson..",
                "tools.jackson..",
                "org.mapstruct..",
                "reactor.."
            )
            .because("Command and result models should remain simple application data contracts");

    @ArchTest
    static final ArchRule APPLICATION_SHOULD_NOT_DEPEND_ON_LOGGING_FRAMEWORKS =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.slf4j..", "ch.qos.logback..")
            .because("Operational logging belongs in adapters and infrastructure, not application use-case logic");

    @ArchTest
    static final ArchRule ADAPTER_OUT_SHOULD_NOT_ACCESS_REQUEST_CONTEXT_DIRECTLY =
        noClasses()
            .that().resideInAPackage("..adapter.out..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..infrastructure.observability.context..",
                "..infrastructure.observability.logging..",
                "..infrastructure.http.propagation.."
            )
            .because("Outbound adapters should receive propagated headers through centralized infrastructure");

    // ==============================================
    // Naming and Shape Rules
    // ==============================================

    @ArchTest
    static final ArchRule INPUT_PORTS_SHOULD_END_WITH_USE_CASE =
        classes()
            .that().resideInAPackage("..application.port.in..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("UseCase")
            .because("Input port names should make use case contracts explicit");

    @ArchTest
    static final ArchRule INPUT_USE_CASES_SHOULD_BE_INTERFACES =
        classes()
            .that().resideInAPackage("..application.port.in..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().beInterfaces()
            .because("Input use cases should be contracts implemented by application services");

    @ArchTest
    static final ArchRule OUTPUT_PORTS_SHOULD_END_WITH_PORT =
        classes()
            .that().resideInAPackage("..application.port.out..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("Port")
            .because("Output port names should make external dependency contracts explicit");

    @ArchTest
    static final ArchRule OUTPUT_PORTS_SHOULD_BE_INTERFACES =
        classes()
            .that().resideInAPackage("..application.port.out..")
            .and().haveSimpleNameEndingWith("Port")
            .should().beInterfaces()
            .because("Output ports should be contracts implemented by outbound adapters");

    @ArchTest
    static final ArchRule DOMAIN_EXCEPTIONS_SHOULD_END_WITH_EXCEPTION =
        classes()
            .that().resideInAPackage("..domain.exception..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .and().areNotAnnotations()
            .and().areNotMemberClasses()
            .should().haveSimpleNameEndingWith("Exception")
            .because("Domain exception names should make domain failures explicit");

    @ArchTest
    static final ArchRule INPUT_COMMANDS_SHOULD_END_WITH_COMMAND =
        classes()
            .that().resideInAPackage("..application.port.in.command..")
            .and().areNotInterfaces()
            .and().areNotMemberClasses()
            .should().haveSimpleNameEndingWith("Command")
            .because("Input command models should be named consistently");

    @ArchTest
    static final ArchRule INPUT_RESULTS_SHOULD_END_WITH_RESULT =
        classes()
            .that().resideInAPackage("..application.port.in.result..")
            .and().areNotInterfaces()
            .and().areNotMemberClasses()
            .should().haveSimpleNameEndingWith("Result")
            .because("Input result models should be named consistently");

    @ArchTest
    static final ArchRule DOMAIN_MODELS_SHOULD_NOT_HAVE_SETTERS =
        noMethods()
            .that().areDeclaredInClassesThat()
            .resideInAPackage("..domain.model..")
            .should().haveNameStartingWith("set")
            .because("Domain models should be immutable");

    // ==============================================
    // Package Structure Rules
    // ==============================================

    @ArchTest
    static final ArchRule APPLICATION_CLASSES_SHOULD_STAY_IN_KNOWN_APPLICATION_PACKAGES =
        classes()
            .that().resideInAPackage("..application..")
            .should().resideInAnyPackage(
                "..application.service..",
                "..application.error..",
                "..application.port.in..",
                "..application.port.in.command..",
                "..application.port.in.result..",
                "..application.port.out..",
                "..application.exception.."
            )
            .because("Application code should be organized by services, ports, models, and exceptions");

    @ArchTest
    static final ArchRule ADAPTER_CLASSES_SHOULD_STAY_IN_ADAPTER_PACKAGES =
        classes()
            .that().resideInAPackage("..adapter..")
            .should().resideInAnyPackage(
                "..adapter.in..",
                "..adapter.out.."
            )
            .because("Adapters should be organized by inbound and outbound direction");

    @ArchTest
    static final ArchRule INFRASTRUCTURE_CLASSES_SHOULD_STAY_IN_KNOWN_INFRASTRUCTURE_PACKAGES =
        classes()
            .that().resideInAPackage("..infrastructure..")
            .should().resideInAnyPackage(
                "..infrastructure.configuration..",
                "..infrastructure.http..",
                "..infrastructure.observability.."
            )
            .because("Infrastructure code should stay grouped by technical concern");

    // ==============================================
    // Cycle Rules
    // ==============================================

    @ArchTest
    static final ArchRule DOMAIN_PACKAGES_SHOULD_NOT_HAVE_CYCLES =
        slices()
            .matching("..domain.(*)..")
            .should().beFreeOfCycles()
            .because("Domain package cycles make business code harder to reason about");

    @ArchTest
    static final ArchRule APPLICATION_PACKAGES_SHOULD_NOT_HAVE_CYCLES =
        slices()
            .matching("..application.(*)..")
            .should().beFreeOfCycles()
            .because("Application package cycles make use-case orchestration harder to reason about");

    @ArchTest
    static final ArchRule ADAPTER_PACKAGES_SHOULD_NOT_HAVE_CYCLES =
        slices()
            .matching("..adapter.(*)..")
            .should().beFreeOfCycles()
            .because("Adapter package cycles indicate coupling between technical adapters");
}
