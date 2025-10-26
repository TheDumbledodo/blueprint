plugins {
    java
    com.gradleup.shadow
}

tasks {
    shadowJar {
        destinationDirectory = rootProject.layout.buildDirectory.dir("jars")
        archiveFileName = "blueprint-${project.name}-${ext["fullVersion"]}.jar"

        dependencies {
            exclude(dependency("com.google.code.gson:gson:.*"))
        }

        mergeServiceFiles()
        exclude("*.properties")
    }

    assemble {
        dependsOn(shadowJar)
    }
}