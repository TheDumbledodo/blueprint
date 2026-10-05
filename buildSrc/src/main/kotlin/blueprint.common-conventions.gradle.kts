plugins {
    java
    jacoco
    com.gradleup.shadow
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

val libs = the<VersionCatalogsExtension>().named("libs")

configurations.named("testImplementation") {
    extendsFrom(configurations.getByName("shadow"))
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    testCompileOnly("org.projectlombok:lombok:1.18.48")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.48")

    compileOnly("org.jetbrains:annotations:23.0.0")

    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
        options.compilerArgs.add("-parameters")
    }

    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)
    }

    shadowJar {
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
        archiveFileName = "blueprint-${project.name}-${ext["fullVersion"]}.jar"
        archiveClassifier = null

        dependencies {
            exclude(dependency("com.google.code.gson:gson:.*"))
            exclude(dependency("net.kyori:.*:.*"))
        }

        mergeServiceFiles()
        exclude("*.properties")
    }

    assemble {
        dependsOn(shadowJar)
    }
}
