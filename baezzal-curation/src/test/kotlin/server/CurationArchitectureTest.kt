package server

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController

@AnalyzeClasses(
    packages = ["server"],
    importOptions = [
        ImportOption.DoNotIncludeTests::class,
        ImportOption.DoNotIncludeJars::class,
        ImportOption.DoNotIncludeArchives::class,
    ],
)
class CurationArchitectureTest {
    @ArchTest
    val curationLayerDependencies: ArchRule =
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Presentation")
            .definedBy("server..presentation..")
            .layer("Application")
            .definedBy("server..application..")
            .layer("Implementation")
            .definedBy("server..implementation..")
            .layer("Infrastructure")
            .definedBy("server..infrastructure..")
            .layer("Domain")
            .definedBy("server..domain..")
            .whereLayer("Presentation")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("Application")
            .mayOnlyBeAccessedByLayers("Presentation")
            .whereLayer("Implementation")
            .mayOnlyBeAccessedByLayers("Application")
            .whereLayer("Infrastructure")
            .mayOnlyBeAccessedByLayers("Implementation")
            .whereLayer(
                "Domain",
            ).mayOnlyBeAccessedByLayers("Application", "Implementation", "Infrastructure")

    @ArchTest
    val classesShouldResideInAllowedLayerPackages: ArchRule =
        classes()
            .that()
            .resideInAPackage("server..")
            .should()
            .resideInAnyPackage(
                "server..presentation..",
                "server..application..",
                "server..implementation..",
                "server..infrastructure..",
                "server..domain..",
                "server..error..",
            )

    @ArchTest
    val presentationClassesShouldEndWithController: ArchRule =
        classes()
            .that()
            .resideInAPackage("server..presentation..")
            .and()
            .areAnnotatedWith(RestController::class.java)
            .should()
            .haveSimpleNameEndingWith("Controller")

    @ArchTest
    val presentationShouldNotDependOnDomain: ArchRule =
        classes()
            .that()
            .resideInAPackage("server..presentation..")
            .should()
            .onlyDependOnClassesThat()
            .resideOutsideOfPackage("server..domain..")

    @ArchTest
    val applicationServiceClassesShouldEndWithService: ArchRule =
        classes()
            .that()
            .resideInAPackage("server..application..")
            .and()
            .areAnnotatedWith(Service::class.java)
            .should()
            .haveSimpleNameEndingWith("Service")

    @ArchTest
    val infrastructureComponentClassesShouldEndWithCache: ArchRule =
        classes()
            .that()
            .resideInAPackage("server..infrastructure..")
            .and()
            .areAnnotatedWith(Component::class.java)
            .should()
            .haveSimpleNameEndingWith("Cache")
            .orShould()
            .haveSimpleNameEndingWith("Storage")

    @ArchTest
    val infrastructureInterfaceClassesShouldEndWithRepository: ArchRule =
        classes()
            .that()
            .resideInAPackage("server..infrastructure..")
            .and()
            .areInterfaces()
            .should()
            .haveSimpleNameEndingWith("Repository")
}
