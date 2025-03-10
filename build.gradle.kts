// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.app.cash.sqldelight) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    id("com.github.ben-manes.versions") version "0.51.0"
}