plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    implementation(project(":api", "shadow"))
    implementation(project(":helper", "shadow"))
    implementation(project(":configuration", "shadow"))
    implementation(project(":configuration:yaml-configuration", "shadow"))
    implementation(project(":configuration:json-configuration", "shadow"))
    implementation(project(":command", "shadow"))

    compileOnly(libs.velocity)

    testImplementation(libs.velocity)
    testImplementation(libs.mockito)
}
