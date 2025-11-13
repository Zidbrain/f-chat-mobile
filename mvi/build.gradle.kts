plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.zidbrain.fchat"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    androidTarget()
    sourceSets {
        val androidMain by getting
        val commonMain by getting {
            dependencies {
                api(libs.androidx.lifecycle.viewmodel)
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}