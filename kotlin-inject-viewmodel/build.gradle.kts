import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "io.github.zidbrain.kotlin.inject.viewmodel"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.runtime)
            implementation(libs.kmp.viewmodel)
            implementation(libs.kotlin.inject.runtime)
        }
    }
}