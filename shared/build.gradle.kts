@file:Suppress("OPT_IN_IS_NOT_ENABLED")

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    android()

    iosArm64 {
        val socketIOFramework = File(rootDir, "network/libs/Ios/socket_IO/ios-arm64").absolutePath
        val rocketFramework = File(rootDir, "network/libs/Ios/SocketRocket/ios-arm64").absolutePath

        val socketFrameworkCompilerLinkerOpts = listOf("-framework", "socket_IO", "-F$socketIOFramework")
        val rocketFrameworkCompilerLinkerOpts = listOf("-framework", "socket_IO", "-F$rocketFramework")

        binaries.framework {
            baseName = "Shared"

            freeCompilerArgs += listOf(
                "-linker-option", "-framework", "-linker-option", "Metal",
                "-linker-option", "-framework", "-linker-option", "CoreText",
                "-linker-option", "-framework", "-linker-option", "CoreGraphics",
            )

            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"

            embedBitcode = org.jetbrains.kotlin.gradle.plugin.mpp.Framework.BitcodeEmbeddingMode.DISABLE

            linkerOpts(socketFrameworkCompilerLinkerOpts)
            linkerOpts(rocketFrameworkCompilerLinkerOpts)

            isStatic = false
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)

                implementation("io.ktor:ktor-client-core:2.1.3")

                implementation("com.russhwolf:multiplatform-settings:1.0.0-RC")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation("co.touchlab:kermit:1.2.2")

                implementation(project(":network"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(fileTree(mapOf("dir" to "libs/android", "include" to listOf("*.jar"))))

                /*
                implementation("com.google.code.gson:gson:2.8.2")
                implementation("com.vividsolutions:jts:1.13")
                 */

                implementation(project(":3dviewer"))
            }
        }

        val iosArm64Main by getting
        val uikitX64Main by creating {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this)
        }
    }
}

android {
    namespace = "com.yama.marshal"

    compileSdk = 33

    defaultConfig {
        targetSdk = 33
        minSdk = 23

        ndk.abiFilters.apply {
            add("armeabi-v7a")
            add("x86")
            add("armeabi")
            add("mips")
            add("arm64-v8a")
        }

        version = Config.version_name
    }

    sourceSets["main"].apply {
        res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    }
}