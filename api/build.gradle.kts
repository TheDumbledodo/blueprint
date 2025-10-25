plugins {
    java
    blueprint.`shadow-conventions`
    blueprint.`publish-conventions`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.classgraph)
}