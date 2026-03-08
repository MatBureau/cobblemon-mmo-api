plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

allprojects {
    group = "com.cobblemon.mmo"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}
