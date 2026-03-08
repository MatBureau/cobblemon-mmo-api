plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    application
}

group = "com.cobblemon.mmo"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("com.cobblemon.mmo.api.ApplicationKt")
}

tasks.shadowJar {
    archiveFileName.set("api-all.jar")
    mergeServiceFiles()
}

dependencies {
    implementation(project(":common"))

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content-negotiation)
    implementation(libs.ktor.serialization.kotlinx-json)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call-logging)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.rate.limit)

    // Exposed ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.exposed.json)

    // Database
    implementation(libs.postgresql)
    implementation(libs.hikari)

    // Redis
    implementation(libs.lettuce.core)

    // DI
    implementation(libs.koin.ktor)
    implementation(libs.koin.core)

    // Logging
    implementation(libs.logback.classic)

    // KotlinX
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactive)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Test
    testImplementation(libs.ktor.server.test-host)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}
