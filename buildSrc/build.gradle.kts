plugins {
    java
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.shadow)
    compileOnly(libs.jetbrains.annotations)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}