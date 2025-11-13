
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.ksp)
}

val propertiesFile = project.file("secrets.properties")
val apikeyProperties = Properties()
apikeyProperties.load(FileInputStream(propertiesFile))

kotlin {
    jvmToolchain(22)
}

android {
    namespace = "io.github.zidbrain.fchat.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.zidbrain.fchat"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "SERVER_CLIENT_ID", apikeyProperties["serverClientId"].toString())
    }

    flavorDimensions += "server"
    productFlavors {
        create("local") {
            buildConfigField("String", "SERVER_URL", "\"http://localhost:8080\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeCompiler {
        includeSourceInformation = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

dependencies {
    implementation(projects.common)

    // credentials
    implementation(libs.androidx.credentials.play.services.auth)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // mockito
    testImplementation(libs.mockito.core)

    // ktor
    implementation(ktor.client.core)
    implementation(ktor.client.cio)
    implementation(ktor.client.logging)
    implementation(ktor.client.contentNegotiation)
    implementation(ktor.serialization.kotlinx.json)
    implementation(ktor.serialization.kotlinx.protobuf)
    implementation(ktor.client.auth)

    // kotlin-inject
    implementation(projects.kotlinInjectViewmodel)
    implementation(libs.kotlin.inject.runtime)
    ksp(libs.kotlin.inject.compiler)

    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}