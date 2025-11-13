import com.android.build.api.dsl.androidLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.app.cash.sqldelight)
    alias(libs.plugins.android.kotlin.multiplatform)
    alias(libs.plugins.ksp)
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
        optIn.add("kotlin.time.ExperimentalTime")
    }

    androidLibrary {
        namespace = "io.github.zidbrain.fchat.common"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.security.crypto)
            implementation(libs.app.cash.sqldelight.android.driver)
            implementation(libs.androidx.compose.runtime)
            api(libs.androidx.credentials.credentials)
            api(libs.googleid)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.runner)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(ktor.client.core)
            implementation(ktor.client.websockets)
            implementation(ktor.client.contentNegotiation)
            implementation(libs.app.cash.sqldelight.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.core)

            api(projects.mvi)
            implementation(projects.kotlinInjectViewmodel)
            implementation(libs.kotlin.inject.runtime)
        }
    }
}

dependencies {
    ksp(libs.kotlin.inject.compiler)
  //  ksp(projects.kotlinInjectViewmodel.processor)
}