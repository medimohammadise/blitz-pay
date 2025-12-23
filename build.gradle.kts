import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.kotlin.plugin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.flyway) apply false
}

group = "com.elegantsoftware.blitzpay"
version = "0.0.1-SNAPSHOT"
description = "EU Qrcode payment"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    // Spring Modulith
    implementation(libs.spring.modulith.starter.core)
    implementation(libs.spring.modulith.starter.jpa)
    runtimeOnly(libs.spring.modulith.actuator)
    runtimeOnly(libs.spring.modulith.starter.insight)

    // Database
    implementation(libs.postgresql)
    runtimeOnly(libs.h2)

    // Flyway - ADDED
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    // TrueLayer dependencies
    implementation(libs.truelayer.java)
    implementation(libs.truelayer.signing)

    // Springdoc
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    // JWT
    implementation(libs.nimbus.jose.jwt)

    // QR Code generation
    implementation(libs.zxing.core)
    implementation(libs.zxing.javase)

    // Logging
    implementation(libs.kotlin.logging.jvm)

    // Dev tools
    developmentOnly(libs.spring.boot.devtools)

    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // Test dependencies
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.webmvc.test)
    testImplementation(libs.spring.modulith.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.mockito.kotlin)
    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.h2)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${libs.versions.spring.modulith.get()}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    maxHeapSize = "2g"
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    jvmArgs(
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:MaxMetaspaceSize=512m"
    )
    useJUnitPlatform()
}

tasks.test {
    doFirst {
        val mockitoAgent = configurations.testRuntimeClasspath.get().find { it.name.contains("mockito-inline") }
        if (mockitoAgent != null) {
            jvmArgs("-javaagent:${mockitoAgent.absolutePath}")
        }
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}