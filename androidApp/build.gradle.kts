plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.yama.marshal.android"
    compileSdk = 32
    defaultConfig {
        applicationId = "com.yama.marshal"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.compose.ui:ui:1.3.1")
    implementation("androidx.compose.foundation:foundation:1.3.1")
    implementation("androidx.activity:activity-compose:1.6.1")
}