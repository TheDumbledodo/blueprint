dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

rootProject.name = "blueprint"

include("api", "helper", "configuration", "command", "menu", "paper", "velocity")
include("configuration:yaml-configuration", "configuration:json-configuration")
include("menu:packet-menu", "menu:bukkit-menu")
