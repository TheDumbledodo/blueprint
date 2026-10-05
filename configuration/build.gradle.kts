plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    shadow(project(":api", "shadow"))
    shadow(project(":helper", "shadow"))
}
