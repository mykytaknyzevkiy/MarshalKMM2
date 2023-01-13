plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
}

kotlin {
    android()

    iosArm64 {
        val socketIOFramework = File(rootDir, "network/libs/Ios/socket_IO/ios-arm64").absolutePath
        val rocketFramework = File(rootDir, "network/libs/Ios/SocketRocket/ios-arm64").absolutePath

        val socketFrameworkCompilerLinkerOpts = listOf("-framework", "socket_IO", "-F$socketIOFramework")
        val rocketFrameworkCompilerLinkerOpts = listOf("-framework", "socket_IO", "-F$rocketFramework")

        compilations.getByName("main") {
            val socketIO by cinterops.creating {
                // Path to .def file
                defFile("libs/Ios/socketIO.def")

                compilerOpts(socketFrameworkCompilerLinkerOpts)
                compilerOpts(rocketFrameworkCompilerLinkerOpts)
            }
        }

        binaries.framework {
            isStatic = true

            linkerOpts(socketFrameworkCompilerLinkerOpts)
            linkerOpts(rocketFrameworkCompilerLinkerOpts)
        }
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
                implementation(
                    fileTree(
                        mapOf(
                            "dir" to "libs/android",
                            "include" to listOf("*.jar")
                        )
                    )
                )
            }
        }

        val iosArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)

            iosArm64Main.dependsOn(this)

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