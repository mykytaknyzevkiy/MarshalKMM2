plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 23
        targetSdk = 33
    }
}

dependencies {
    implementation("joda-time:joda-time:2.9.4")
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("androidx.activity:activity-compose:1.7.0-alpha02")
    implementation("com.android.support:support-annotations:28.0.0")
    implementation("org.jetbrains:annotations-java5:15.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.vividsolutions:jts:1.13")
}
