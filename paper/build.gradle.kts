plugins {
    java
    blueprint.`shadow-conventions`
    blueprint.`publish-conventions`
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    shadow(project(":api", "shadow"))
    shadow(project(":configuration", "shadow"))
    compileOnly(libs.paper)
    implementation(libs.acf.paper)
}