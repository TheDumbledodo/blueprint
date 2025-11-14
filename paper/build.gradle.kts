plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    implementation(project(":api"))
    implementation(project(":helper"))
    implementation(project(":configuration", "shadow"))

    compileOnly(libs.paper)
    implementation(libs.acf.paper)
}