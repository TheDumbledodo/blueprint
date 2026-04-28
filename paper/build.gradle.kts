plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    implementation(project(":api", "shadow"))
    implementation(project(":helper", "shadow"))
    implementation(project(":configuration", "shadow"))

    compileOnly(libs.paper)
    implementation(libs.acf.paper)
}
