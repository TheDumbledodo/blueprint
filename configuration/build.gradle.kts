plugins {
    java
    blueprint.`shadow-conventions`
    blueprint.`publish-conventions`
}

repositories {
    mavenCentral()
    maven("https://projectlombok.org/edge-releases")
}

dependencies {
    shadow(project(":api", "shadow"))
    implementation(libs.configlib.yaml)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}