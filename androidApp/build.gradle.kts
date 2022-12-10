plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.yama.marshal_app"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.yama.marshal.app"
        minSdk = 21
        targetSdk = 33
        versionCode = 101
        versionName = "2.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
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
    implementation("androidx.compose.ui:ui:1.4.0-alpha02")
    implementation("androidx.compose.foundation:foundation:1.4.0-alpha02")
    implementation("androidx.activity:activity-compose:1.7.0-alpha02")
}