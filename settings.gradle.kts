pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
    }
}

rootProject.name = "MarshalKMM"
include(":shared")
include(":androidApp")
include(":viewer")