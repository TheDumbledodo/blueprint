plugins {
    java
    blueprint.`common-conventions`
    blueprint.`publish-conventions`
}

dependencies {
    shadow(libs.bundles.adventure)
    shadow(libs.bundles.adventure.serializers)
}