rootProject.name = "cobblemon-mmo-backend"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

include(":common")
include(":api")
