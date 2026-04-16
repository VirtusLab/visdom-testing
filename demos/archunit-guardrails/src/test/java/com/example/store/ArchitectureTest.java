package com.example.store;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

@AnalyzeClasses(packages = "com.example.store", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories =
            noClasses().that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..repository..")
                    .because("Controllers must go through the service layer");

    @ArchTest
    static final ArchRule no_field_injection =
            noFields().should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                    .because("Use constructor injection for testability");

    @ArchTest
    static final ArchRule no_rest_template =
            noClasses().should().dependOnClassesThat()
                    .haveFullyQualifiedName("org.springframework.web.client.RestTemplate")
                    .because("RestTemplate is deprecated since Spring 6.1; use RestClient");

    @ArchTest
    static final ArchRule no_generic_exceptions =
            noClasses().that().resideInAPackage("..service..")
                    .should().dependOnClassesThat()
                    .haveFullyQualifiedName("java.lang.RuntimeException")
                    .because("Use specific exception types, not generic RuntimeException");

    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
            noClasses().that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..controller..")
                    .because("Services must not depend on controllers — this creates cyclic dependencies");
}
