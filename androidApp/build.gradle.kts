plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\neka1\\Documents\\YamaTrack\\app\\rsa\\ytr.keystore")
            storePassword = "igolfdev591"
            keyAlias = "android"
            keyPassword = "igolfdev591"
        }
    }
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
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.compose.ui:ui:1.4.0-alpha03")
    implementation("androidx.compose.foundation:foundation:1.4.0-alpha03")
    implementation("androidx.activity:activity-compose:1.7.0-alpha02")
}