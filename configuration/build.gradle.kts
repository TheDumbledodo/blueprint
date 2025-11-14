plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    shadow(project(":api"))
    implementation(libs.configlib.yaml)
}