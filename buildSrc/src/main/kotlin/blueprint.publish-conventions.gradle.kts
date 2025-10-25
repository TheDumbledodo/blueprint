import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            val shadowJarTask =
                tasks.findByName("shadowJar") as? ShadowJar

            if (shadowJarTask != null) {
                artifact(shadowJarTask.archiveFile) { classifier = null }

            } else {
                from(components["java"])
            }

            groupId = rootProject.group.toString()
            artifactId = "${rootProject.name}-${project.name}"
            version = rootProject.version.toString()
        }
    }
}