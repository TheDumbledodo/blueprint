plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    implementation(project(":api"))
    implementation(project(":helper"))
    implementation(project(":configuration"))

    compileOnly(libs.velocity)
    implementation(libs.acf.velocity)
}