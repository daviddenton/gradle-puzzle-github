dependencyResolutionManagement {
    repositories.mavenCentral()
    repositories.gradlePluginPortal()

    versionCatalogs.create("libs") {
        from(files("../libs.versions.toml"))
    }
}

include("publication")
