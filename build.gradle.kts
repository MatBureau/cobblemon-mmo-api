plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
    id("com.github.johnrengelman.shadow") version "8.3.6"
}

group = "com.cobblemon.mmo"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass.set("com.cobblemon.mmo.ApplicationKt")
}

tasks.shadowJar {
    archiveFileName.set("app.jar")
    mergeServiceFiles()
}

repositories {
    mavenCentral()
}

val ktorVersion = "3.1.1"

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.5.15")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
