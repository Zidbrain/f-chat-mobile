import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform)
}

kotlin {
    androidLibrary {
        namespace = "io.github.zidbrain.kotlin.inject.viewmodel"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.kotlinInjectViewmodel)
            implementation(libs.symbol.processing.api)
        }
    }
}