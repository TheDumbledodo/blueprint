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
    shadow(libs.bundles.adventure)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}