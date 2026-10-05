plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    shadow(project(":configuration", "shadow"))
    compileOnly(libs.gson)

    testImplementation(libs.gson)
}
