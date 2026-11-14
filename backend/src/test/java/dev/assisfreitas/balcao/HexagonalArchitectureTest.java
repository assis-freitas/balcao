package dev.assisfreitas.balcao;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The hexagon, enforced (docs/adr/0001). Rules are written per layer name, so every new bounded
 * context that follows the layout is covered automatically.
 */
@AnalyzeClasses(
    packages = "dev.assisfreitas.balcao",
    importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  @ArchTest
  static final ArchRule domainIsPlainJava =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..", "jakarta..", "org.hibernate..")
          .as("the domain is plain Java: no Spring, JPA or Hibernate");

  @ArchTest
  static final ArchRule domainDependsOnNothingOutward =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..application..", "..adapter..");

  @ArchTest
  static final ArchRule applicationDoesNotKnowAdapters =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter..");

  @ArchTest
  static final ArchRule applicationOnlyBorrowsTransactionsFromSpring =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat(
              resideInAnyPackage("org.springframework..", "jakarta..")
                  .and(not(resideInAPackage("org.springframework.transaction.annotation"))))
          .because("use cases may use @Transactional and nothing else from the framework");

  @ArchTest
  static final ArchRule drivingAdaptersDoNotTouchDrivenAdapters =
      noClasses()
          .that()
          .resideInAPackage("..adapter.in..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter.out..");

  @ArchTest
  static final ArchRule drivingAdaptersGoThroughIncomingPorts =
      noClasses()
          .that()
          .resideInAPackage("..adapter.in..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..application.port.out..");

  @ArchTest
  static final ArchRule jpaStaysInPersistenceAdapters =
      classes()
          .that()
          .areAnnotatedWith(jakarta.persistence.Entity.class)
          .should()
          .resideInAPackage("..adapter.out.persistence..");
}
