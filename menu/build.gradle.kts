plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    implementation(project(":api", "shadow"))
    implementation(project(":helper", "shadow"))

    shadow(libs.bundles.adventure)
    shadow(libs.bundles.adventure.serializers)

    compileOnly(libs.paper)

    compileOnly(libs.packetevents.spigot)
}