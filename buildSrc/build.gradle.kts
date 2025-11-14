plugins {
    java
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()

    maven("https://projectlombok.org/edge-releases")
}

dependencies {
    implementation(libs.shadow)
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}