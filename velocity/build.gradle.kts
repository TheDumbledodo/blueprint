plugins {
    java
    blueprint.`shadow-conventions`
    blueprint.`publish-conventions`
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    implementation(project(":api", "shadow"))
    implementation(project(":configuration"))

    compileOnly(libs.velocity)
    implementation(libs.acf.velocity)
}