import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    kotlin("plugin.jpa") version "2.3.0"
    id("org.springframework.boot") version "3.5.10"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("dev.detekt") version "2.0.0-alpha.2"
}

allprojects {
    group = ""
    version = "0.0.1"

    repositories {
        mavenCentral()
    }
}

subprojects {
    if (!buildFile.exists()) {
        return@subprojects
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "dev.detekt")

    dependencies {
        // Spring & Kotlin
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

        // logging
        implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")

        // test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.kotest:kotest-assertions-core:5.7.2")
        testImplementation("io.mockk:mockk:1.12.4")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
        testImplementation("com.tngtech.archunit:archunit-junit5:1.4.2")
    }

    tasks.test {
        useJUnitPlatform()
    }

    configure<KtlintExtension> {
        version.set("1.7.1")
        verbose.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
        }
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }

    extensions.configure<DetektExtension> {
        toolVersion = "2.0.0-alpha.2"
        config.setFrom(rootProject.file("detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        ignoreFailures = false
        source.setFrom("src/main/kotlin", "src/test/kotlin")
        basePath.set(rootDir)
    }

    kotlin {
        jvmToolchain(21)
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "21"
        reports {
            checkstyle.required.set(true)
            html.required.set(true)
            sarif.required.set(true)
        }
    }

    tasks.named("bootJar") {
        enabled = false
    }

    tasks.named("jar") {
        enabled = true
    }
}

tasks.register("detektAll") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs detekt for all subprojects with source files."
    dependsOn(
        subprojects
            .filter { it.buildFile.exists() }
            .map { "${it.path}:detekt" },
    )
}

tasks.register("lintKotlin") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs ktlint and detekt across the project."
    dependsOn("ktlintCheck", "detektAll")
}

tasks.register("formatKotlin") {
    group = "formatting"
    description =
        "Formats Kotlin sources and Gradle Kotlin scripts with ktlint."
    dependsOn("ktlintFormat")
}
dependencies {
    implementation(kotlin("stdlib"))
}
repositories {
    mavenCentral()
}
