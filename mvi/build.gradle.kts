@file:Suppress("OPT_IN_USAGE")

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.zidbrain.fchat"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    androidTarget()
    jvmToolchain(22)
    sourceSets {
        val androidMain by getting {
            dependencies {
                api(libs.androidx.lifecycle.viewmodel)
            }
        }
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}