plugins {
    java
    blueprint.`shadow-conventions`
    blueprint.`publish-conventions`
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://projectlombok.org/edge-releases")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":helper"))
    implementation(project(":configuration", "shadow"))

    compileOnly(libs.paper)
    implementation(libs.acf.paper)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}