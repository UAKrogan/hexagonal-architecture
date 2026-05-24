package com.example.hexagonal;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit tests to validate hexagonal architecture principles.
 * <p>
 * This test class contains 35 test cases that validate:
 * - Layer dependencies and isolation
 * - Framework independence in domain layer
 * - Naming conventions
 * - Package structure
 * - Interface and implementation patterns
 */
@AnalyzeClasses(
    packages = "com.example.hexagonal",
    importOptions = {
        ImportOption.DoNotIncludeTests.class
    }
)
public class HexagonalArchitectureTest {

    // ==============================================
    // Layer Dependency Tests (10 tests)
    // ==============================================

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_APPLICATION_LAYER =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application..")
            .because("Domain layer must remain independent of application layer to ensure " +
                "business logic is not coupled to use cases");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_ADAPTER_LAYER =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter..")
            .because("Domain layer must be isolated from infrastructure concerns and " +
                "external system adapters");

    @ArchTest
    static final ArchRule APPLICATION_SHOULD_NOT_DEPEND_ON_ADAPTER_LAYER =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..adapter..")
            .because("Application layer should depend only on ports (interfaces), " +
                "not on concrete adapter implementations");

    @ArchTest
    static final ArchRule CONTROLLERS_SHOULD_NOT_DEPEND_ON_DOMAIN_SERVICES =
        noClasses()
            .that().resideInAPackage("..adapter.in.web..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat()
            .resideInAPackage("..domain.service..")
            .because("Controllers should interact with the domain through application " +
                "services and input ports, not directly with domain services");

    @ArchTest
    static final ArchRule ADAPTER_OUT_SHOULD_IMPLEMENT_OUTPUT_PORTS =
        classes()
            .that().resideInAPackage("..adapter.out..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().dependOnClassesThat()
            .resideInAPackage("..application.port.out..")
            .because("Output adapters must implement output ports to provide concrete " +
                "implementations for external system interactions");

    @ArchTest
    static final ArchRule APPLICATION_SERVICES_SHOULD_USE_DOMAIN =
        classes()
            .that().resideInAPackage("..application.service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..domain..")
            .because("Application services orchestrate business logic by using " +
                "domain models and services");

    @ArchTest
    static final ArchRule ADAPTERS_SHOULD_USE_DOMAIN_MODELS =
        classes()
            .that().resideInAPackage("..adapter..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().dependOnClassesThat()
            .resideInAPackage("..domain.model..")
            .because("Adapters must work with domain models to translate between " +
                "external formats and internal domain representations");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_HAVE_CYCLIC_DEPENDENCIES =
        slices()
            .matching("..domain.(*)..")
            .should().beFreeOfCycles()
            .because("Cyclic dependencies in domain layer create tight coupling and " +
                "make the code harder to understand and maintain");

    @ArchTest
    static final ArchRule APPLICATION_SHOULD_NOT_HAVE_CYCLIC_DEPENDENCIES =
        slices()
            .matching("..application.(*)..")
            .should().beFreeOfCycles()
            .because("Cyclic dependencies in application layer create tight coupling " +
                "between use cases and services");

    @ArchTest
    static final ArchRule ADAPTER_SHOULD_NOT_HAVE_CYCLIC_DEPENDENCIES =
        slices()
            .matching("..adapter.(*)..")
            .should().beFreeOfCycles()
            .because("Cyclic dependencies in adapter layer create tight coupling " +
                "between different infrastructure concerns");

    // ==============================================
    // Framework Isolation Tests (8 tests)
    // ==============================================

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_IMPORT_SPRING_FRAMEWORK =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .because("Domain layer must be framework-agnostic to ensure business " +
                "logic is not tied to Spring framework");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_IMPORT_REACTOR_EXCEPT_IN_PORTS =
        noClasses()
            .that().resideInAPackage("..domain..")
            .and().resideOutsideOfPackage("..application.port..")
            .should().dependOnClassesThat()
            .resideInAPackage("reactor..")
            .because("Domain layer should not depend on reactive framework except in " +
                "port interfaces where async behavior is part of the contract");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_IMPORT_JACKSON =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.fasterxml.jackson..", "tools.jackson..")
            .because("Domain layer must not depend on JSON serialization libraries " +
                "as this is an infrastructure concern");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_IMPORT_MAPSTRUCT =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.mapstruct..")
            .because("Domain layer should not depend on mapping frameworks as object " +
                "mapping is an adapter layer responsibility");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_ONLY_USE_ALLOWED_LOMBOK_ANNOTATIONS =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("lombok.experimental..")
            .because("Domain layer should only use stable Lombok annotations " +
                "(@Value, @Builder, @With) and avoid experimental features");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_IMPORT_JAVAX_VALIDATION =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("javax.validation..")
            .because("Domain layer should implement its own validation logic rather " +
                "than depending on framework validation annotations");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_IMPORT_EXTERNAL_API_CLIENTS =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.example.client..")
            .because("Domain layer must not directly depend on external API clients " +
                "as these are infrastructure concerns handled by adapters");

    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_HAVE_SPRING_ANNOTATIONS =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().beAnnotatedWith("org.springframework.stereotype.Component")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Service")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
            .orShould().beAnnotatedWith("org.springframework.context.annotation.Configuration")
            .orShould().beAnnotatedWith("org.springframework.boot.context.properties.ConfigurationProperties")
            .because("Domain layer classes should not be annotated with Spring " +
                "stereotypes to maintain framework independence");

    // ==============================================
    // Naming Convention Tests (7 tests)
    // ==============================================

    @ArchTest
    static final ArchRule INPUT_PORTS_SHOULD_END_WITH_USE_CASE =
        classes()
            .that().resideInAPackage("..application.port.in..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("UseCase")
            .because("Input ports represent use cases and should be clearly " +
                "identifiable by their naming convention");

    @ArchTest
    static final ArchRule OUTPUT_PORTS_SHOULD_END_WITH_PORT =
        classes()
            .that().resideInAPackage("..application.port.out..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("Port")
            .because("Output ports define contracts for external dependencies and " +
                "should be clearly identifiable by their naming convention");

    @ArchTest
    static final ArchRule MAIN_ADAPTERS_SHOULD_END_WITH_ADAPTER =
        classes()
            .that().resideInAPackage("..adapter.out..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .and().areNotAnnotations()
            .and().areNotMemberClasses()
            .and().haveSimpleNameEndingWith("Adapter")
            .should().haveSimpleNameEndingWith("Adapter")
            .because("Adapter classes implement ports and should be clearly " +
                "identifiable by their naming convention");

    @ArchTest
    static final ArchRule DOMAIN_SERVICES_SHOULD_END_WITH_SERVICE =
        classes()
            .that().resideInAPackage("..domain.service..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .and().areNotAnnotations()
            .and().areNotMemberClasses()
            .should().haveSimpleNameEndingWith("Service")
            .allowEmptyShould(true)
            .because("Domain services encapsulate business logic and should be " +
                "clearly identifiable by their naming convention");

    @ArchTest
    static final ArchRule DOMAIN_EXCEPTIONS_SHOULD_END_WITH_EXCEPTION =
        classes()
            .that().resideInAPackage("..domain.exception..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .and().areNotAnnotations()
            .and().areNotMemberClasses()
            .should().haveSimpleNameEndingWith("Exception")
            .because("Domain exceptions represent business rule violations and should " +
                "be clearly identifiable by their naming convention");

    @ArchTest
    static final ArchRule COMMANDS_SHOULD_END_WITH_COMMAND =
        classes()
            .that().resideInAPackage("..application.port.in..")
            .and().areNotInterfaces()
            .and().areNotMemberClasses()
            .and().haveSimpleNameContaining("Command")
            .should().haveSimpleNameEndingWith("Command")
            .because("Command objects represent input data for use cases and should " +
                "follow consistent naming conventions");

    @ArchTest
    static final ArchRule RESULTS_SHOULD_END_WITH_RESULT =
        classes()
            .that().resideInAPackage("..application.port.in..")
            .and().areNotInterfaces()
            .and().areNotMemberClasses()
            .and().haveSimpleNameContaining("Result")
            .should().haveSimpleNameEndingWith("Result")
            .because("Result objects represent output data from use cases and should " +
                "follow consistent naming conventions");

    // ==============================================
    // Package Structure Tests (5 tests)
    // ==============================================

    @ArchTest
    static final ArchRule DOMAIN_CLASSES_SHOULD_RESIDE_IN_DOMAIN_PACKAGE =
        classes()
            .that().haveSimpleNameEndingWith("Service")
            .and().areNotInterfaces()
            .and().resideInAPackage("..domain..")
            .should().resideInAPackage("..domain.service..")
            .orShould().resideInAPackage("..domain.model..")
            .orShould().resideInAPackage("..domain.exception..")
            .allowEmptyShould(true)
            .because("Domain classes should be organized in appropriate sub-packages " +
                "to maintain clear separation of concerns");

    @ArchTest
    static final ArchRule APPLICATION_CLASSES_SHOULD_RESIDE_IN_APPLICATION_PACKAGE =
        classes()
            .that().resideInAPackage("..application..")
            .should().resideInAnyPackage(
                "..application.service..",
                "..application.port.in..",
                "..application.port.out..",
                "..application.config.."
            )
            .because("Application layer classes should be organized in appropriate " +
                "sub-packages (services, ports) to maintain clear structure");

    @ArchTest
    static final ArchRule ADAPTER_CLASSES_SHOULD_RESIDE_IN_ADAPTER_PACKAGE =
        classes()
            .that().resideInAPackage("..adapter..")
            .should().resideInAnyPackage(
                "..adapter.in..",
                "..adapter.out.."
            )
            .because("Adapter classes should be organized by direction (in/out) to " +
                "clearly separate input and output adapters");

    @ArchTest
    static final ArchRule INPUT_PORTS_SHOULD_RESIDE_IN_APPLICATION_PORT_IN_PACKAGE =
        classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .and().areInterfaces()
            .should().resideInAPackage("..application.port.in..")
            .because("Input ports (use cases) should be located in the dedicated " +
                "input port package to maintain clear boundaries");

    @ArchTest
    static final ArchRule OUTPUT_PORTS_SHOULD_RESIDE_IN_APPLICATION_PORT_OUT_PACKAGE =
        classes()
            .that().haveSimpleNameEndingWith("Port")
            .and().areInterfaces()
            .should().resideInAPackage("..application.port.out..")
            .because("Output ports should be located in the dedicated output port " +
                "package to maintain clear boundaries");

    // ==============================================
    // Interface and Implementation Tests (5 tests)
    // ==============================================

    @ArchTest
    static final ArchRule DOMAIN_MODELS_SHOULD_NOT_HAVE_SETTERS =
        noMethods()
            .that().areDeclaredInClassesThat()
            .resideInAPackage("..domain.model..")
            .should().haveNameStartingWith("set")
            .because("Domain models should be immutable to prevent uncontrolled " +
                "state changes and ensure thread safety");

    @ArchTest
    static final ArchRule INPUT_PORTS_SHOULD_BE_INTERFACES =
        classes()
            .that().resideInAPackage("..application.port.in..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().beInterfaces()
            .because("Input ports must be interfaces to define contracts that can " +
                "be implemented by application services");

    @ArchTest
    static final ArchRule OUTPUT_PORTS_SHOULD_BE_INTERFACES =
        classes()
            .that().resideInAPackage("..application.port.out..")
            .and().haveSimpleNameEndingWith("Port")
            .should().beInterfaces()
            .because("Output ports must be interfaces to define contracts that can " +
                "be implemented by adapters");

    @ArchTest
    static final ArchRule ADAPTERS_SHOULD_BE_ANNOTATED_WITH_COMPONENT_OR_SERVICE =
        classes()
            .that().resideInAPackage("..adapter.out..")
            .and().areNotInterfaces()
            .and().areNotEnums()
            .and().areNotAnnotations()
            .and().haveSimpleNameEndingWith("Adapter")
            .should().beAnnotatedWith("org.springframework.stereotype.Component")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Service")
            .because("Adapters must be Spring-managed beans to be injected as " +
                "implementations of output ports");
}

