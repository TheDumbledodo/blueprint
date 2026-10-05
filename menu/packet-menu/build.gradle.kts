plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    implementation(project(":api", "shadow"))
    implementation(project(":helper", "shadow"))
    implementation(project(":menu", "shadow"))

    shadow(libs.bundles.adventure)
    shadow(libs.bundles.adventure.serializers)

    compileOnly(libs.packetevents)

    testImplementation(libs.bundles.packetevents.test)
}
