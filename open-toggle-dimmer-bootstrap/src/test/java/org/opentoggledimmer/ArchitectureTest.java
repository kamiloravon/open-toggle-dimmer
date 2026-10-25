package org.opentoggledimmer;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Архитектурные тесты: направление зависимостей между слоями.
 * Architecture tests: dependency direction between layers.
 *
 * <hr>
 *
 * <p>Домен не зависит от application и adapter; application не зависит от
 * adapter; домен и application не используют фреймворки (кроме JDK).
 * The domain does not depend on application or adapter; application does not
 * depend on adapter; domain and application use no frameworks (JDK only).</p>
 */
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("org.opentoggledimmer");
    }

    @Test
    @DisplayName("домен не зависит от application")
    void domainDoesNotDependOnApplication() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .check(classes);
    }

    @Test
    @DisplayName("домен не зависит от adapter")
    void domainDoesNotDependOnAdapter() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .check(classes);
    }

    @Test
    @DisplayName("application не зависит от adapter")
    void applicationDoesNotDependOnAdapter() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .check(classes);
    }

    @Test
    @DisplayName("домен не использует фреймворки (Spring/JPA/Jackson/Lombok)")
    void domainUsesNoFrameworks() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta..", "javax..", "com.fasterxml..", "lombok..")
                .check(classes);
    }

    @Test
    @DisplayName("домен не зависит от Instant (только OffsetDateTime)")
    void domainDoesNotUseInstant() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().haveFullyQualifiedName("java.time.Instant")
                .check(classes);
    }
}