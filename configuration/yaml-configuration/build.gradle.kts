plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    shadow(project(":configuration", "shadow"))
    implementation(libs.snakeyaml)
}

tasks {
    shadowJar {
        relocate("org.yaml.snakeyaml", "com.github.thedumbledodo.blueprint.libs.snakeyaml")
    }
}
