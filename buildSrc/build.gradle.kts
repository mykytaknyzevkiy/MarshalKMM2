plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

sourceSets.main {
    java {
        setSrcDirs(setOf(projectDir.parentFile.resolve("src/main/kotlin")))
        include("Config.kt")
    }
}