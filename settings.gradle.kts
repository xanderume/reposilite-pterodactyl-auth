rootProject.name = "reposilite-pterodactyl-auth"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        register("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
