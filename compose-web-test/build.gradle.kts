plugins {
    kotlin("multiplatform") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    id("org.jetbrains.compose") version "1.9.3"
}

kotlin {
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)

                implementation(compose.components.resources)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0")

                implementation("io.ktor:ktor-client-core:3.1.3")
                implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

                // 이미지
                implementation("io.coil-kt.coil3:coil-compose:3.3.0")
                implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.1.3")

                implementation(npm("@zxing/browser", "0.1.5"))
                implementation(npm("@zxing/library", "0.21.3"))
            }
        }
    }
}

compose.resources {
    generateResClass = always
    packageOfResClass = "composewebtest.generated.resources"
}