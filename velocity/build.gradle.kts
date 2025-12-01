plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    implementation(project(":api", "shadow"))
    implementation(project(":helper", "shadow"))
    implementation(project(":configuration", "shadow"))
    implementation(project(":menu", "shadow"))

    compileOnly(libs.velocity)
    implementation(libs.acf.velocity)
}