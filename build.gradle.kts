plugins {
    java
}

group = "com.github.thedumbledodo"
description = rootProject.description
version = "${rootProject.ext["fullVersion"]}"

tasks {
    defaultTasks("build")
}

allprojects {
    tasks {
        withType<Jar> {
            archiveBaseName = "${rootProject.name}-${project.name}"
            archiveVersion = version as String
        }
    }
}