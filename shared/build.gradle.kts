@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import org.jetbrains.kotlin.konan.library.konanCommonLibraryPath

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
}

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    android()

    iosArm32 {
        binaries.framework {
            baseName = "shared"
        }
    }
    iosX64("uikitX64") {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics"
                )
            }
            framework {
                baseName = "shared"
            }
        }
    }
    iosArm64("uikitArm64") {
        binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics"
                )
                // TODO: the current compose binary surprises LLVM, so disable checks for now.
                freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
            }
            framework {
                baseName = "shared"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            resources.srcDir("src/commonMain/resources")

            dependencies {
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.runtime)

                implementation("io.ktor:ktor-utils:2.1.3")
            }
        }
        val androidMain by getting

        val iosArm32Main by getting
        val uikitArm64Main by getting
        val uikitX64Main by getting {
            dependsOn(commonMain)
            uikitArm64Main.dependsOn(this)
            iosArm32Main.dependsOn(this)
        }
    }
}

android {
    namespace = "com.yama.marshal"
    compileSdk = 32
    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}

compose.experimental {
    uikit.application {
        bundleIdPrefix = "com.yama.marshal"
        projectName = "Marshal"
        deployConfigurations {
            simulator("IPhone13") {
                device = org.jetbrains.compose.experimental.dsl.IOSDevices.IPHONE_13_PRO
            }
            simulator("IPadUI") {
                device = org.jetbrains.compose.experimental.dsl.IOSDevices.IPAD_MINI_6th_Gen
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
        }
    }
}