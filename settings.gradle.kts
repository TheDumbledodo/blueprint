dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

rootProject.name = "blueprint"
include("api")
include("paper")
include("velocity")
include("helper")
include("configuration")