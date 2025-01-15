
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.kotlinxSerialization)
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
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.zidbrain.fchat"
        minSdk = 24
        targetSdk = 35
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

ksp {
    arg("KOIN_CONFIG_CHECK","true")
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "false")
}

dependencies {
    implementation(project(":common"))

    // credentials
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.credentials.credentials)
    implementation(libs.googleid)

    // firebase
    implementation(platform(libs.firebase.bom))

    // koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android.compose)
    ksp(libs.koin.ksp.compiler)
    testImplementation(libs.koin.test)

    // mockito
    testImplementation(libs.mockito.core)

    // ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.logger)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.content.serialization.json)
    implementation(libs.kotlinx.serialization)
    implementation(libs.ktor.client.auth)

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