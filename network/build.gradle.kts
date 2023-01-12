plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    kotlin("plugin.serialization")
}

kotlin {
    android()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"

        ios.deploymentTarget = "14.1"

        specRepos {
            url("https://github.com/Kotlin/kotlin-cocoapods-spec.git")
        }

        framework {
            baseName = "network"
        }

        pod(name = "Ios", path = File(projectDir, "libs/Ios"))
    }
    
    sourceSets {
        val ktorVersion = "2.1.3"


        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation("co.touchlab:kermit:1.2.2")
                implementation("com.appmattus.crypto:cryptohash:0.10.1")
            }
        }


        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation(fileTree(mapOf("dir" to "libs/android", "include" to listOf("*.jar"))))
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)

            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
    }
}

android {
    namespace = "com.yama.marshal.network"
    compileSdk = 32
    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}