plugins {
    id("com.android.library").version("7.3.1").apply(false)
    kotlin("multiplatform").version("1.7.20").apply(false)
    id("org.jetbrains.compose").version("1.3.0-beta04-dev903").apply(false)
    kotlin("native.cocoapods").version("1.7.20").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.14")
        classpath("com.google.firebase:firebase-appdistribution-gradle:3.1.1")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
    }
}
