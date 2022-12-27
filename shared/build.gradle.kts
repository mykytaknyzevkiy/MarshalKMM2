@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization").version("1.7.20")
    kotlin("native.cocoapods")
}

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    android()

    val xcFramework = XCFramework("Shared")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "Shared"
            freeCompilerArgs += listOf(
                "-linker-option", "-framework", "-linker-option", "Metal",
                "-linker-option", "-framework", "-linker-option", "CoreText",
                "-linker-option", "-framework", "-linker-option", "CoreGraphics"
            )
            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
            embedBitcode = org.jetbrains.kotlin.gradle.plugin.mpp.Framework.BitcodeEmbeddingMode.DISABLE

            xcFramework.add(this)
        }

        /*it.compilations.getByName("main") {
            val ThereDViewer by cinterops.creating {
                // Path to .def file
                defFile("libs/Ios/3DViewer/3DViewer.def")

                includeDirs("libs/Ios/3DViewer/Include")
            }

            it.binaries.all {
                // Linker options required to link to the library.
                linkerOpts("/Users/nek_zabirov/Documents/MarshallKMM/shared/libs/Ios/3DViewer/Include/libiGolfViewer3D.a")
            }
        }*/
    }

    cocoapods {
        ios.deploymentTarget = "14.1"

        version = "1.5"

        summary = "This is sample Summary"
        homepage = "Home URL"

        specRepos {
            url("https://github.com/Kotlin/kotlin-cocoapods-spec.git")
        }

        podfile = project.file("../iosApp/Podfile")

        framework {
            baseName = "Shared"

            isStatic = false

            transitiveExport = false

            this.embedBitcode = org.jetbrains.kotlin.gradle.plugin.mpp.Framework.BitcodeEmbeddingMode.DISABLE
            this.freeCompilerArgs += listOf(
                "-linker-option", "-framework", "-linker-option", "Metal",
                "-linker-option", "-framework", "-linker-option", "CoreText",
                "-linker-option", "-framework", "-linker-option", "CoreGraphics"
            )
            this.freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
        }

        pod(name = "Ios", path = File(projectDir, "libs/Ios"))
    }

    sourceSets {
        val ktorVersion = "2.1.3"

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
                implementation("com.russhwolf:multiplatform-settings:1.0.0-RC")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation("co.touchlab:kermit:1.2.2")
                implementation("com.appmattus.crypto:cryptohash:0.10.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

                implementation(fileTree(mapOf("dir" to "libs/android", "include" to listOf("*.jar"))))

                /*
                implementation("com.google.code.gson:gson:2.8.2")
                implementation("com.vividsolutions:jts:1.13")
                 */

                implementation(project(":viewer"))
            }
        }
        val androidTest by getting

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val uikitX64Main by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "com.yama.marshal"

    compileSdk = 33

    defaultConfig {
        targetSdk = 33
        minSdk = 23
    }

    sourceSets["main"].apply {
        res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    }
}