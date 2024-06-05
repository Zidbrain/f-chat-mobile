@file:Suppress("OPT_IN_USAGE")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.app.cash.sqldelight)
    id("com.android.library")
}

android {
    namespace = "io.github.zidbrain.fchat.common"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        isCoreLibraryDesugaringEnabled = true
    }
    dependencies {
        coreLibraryDesugaring(libs.desugar.jdk.libs)
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("io.github.zidbrain")
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.security.crypto)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.app.cash.sqldelight.android.driver)
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.client.serialization.json)
                implementation(libs.app.cash.sqldelight.coroutines)
            }
        }
    }
}