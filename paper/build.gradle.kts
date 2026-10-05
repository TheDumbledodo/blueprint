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

    compileOnly(libs.paper)

    testImplementation(libs.paper)
    testImplementation(libs.mockbukkit)
}

tasks.withType<JavaCompile> {
    options.release = 25
}
