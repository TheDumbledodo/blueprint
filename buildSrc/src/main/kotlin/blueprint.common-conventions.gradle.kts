plugins {
    java
    com.gradleup.shadow
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://projectlombok.org/edge-releases")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:edge-SNAPSHOT")
    annotationProcessor("org.projectlombok:lombok:edge-SNAPSHOT")

    compileOnly("org.jetbrains:annotations:23.0.0")
}

tasks {
    shadowJar {
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
        archiveFileName = "blueprint-${project.name}-${ext["fullVersion"]}.jar"
        archiveClassifier = null

        dependencies {
            exclude(dependency("com.google.code.gson:gson:.*"))
        }

        mergeServiceFiles()
        exclude("*.properties")
        minimize()
    }

    assemble {
        dependsOn(shadowJar)
    }
}