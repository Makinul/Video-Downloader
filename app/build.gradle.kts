plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
}

android {
    namespace = "com.makinul.instragram.video.downloader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.makinul.instragram.video.downloader"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["usesCleartextTraffic"] = "false"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Specifies one flavor dimension.
    flavorDimensions += "environment"
    productFlavors {
        create("prod") {
            dimension = "environment"
            buildConfigField ("String", "BASE_URL", "\"https://ig-downloader-623725521268.asia-south1.run.app/\"")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
        }
        create("dev") {
            dimension = "environment"
            buildConfigField ("String", "BASE_URL", "\"http://10.0.2.2:5000/\"")
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            versionCode = 30000 + (android.defaultConfig.versionCode ?: 0)
            manifestPlaceholders["usesCleartextTraffic"] = "false"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.media3.exoplayer)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // For Material 3 icons (if not already included)
    implementation(libs.androidx.material.icons.extended.android)

    // Koin core
    implementation(libs.koin.core)
    // Koin for Android
    implementation(libs.koin.android)
    // Koin for Jetpack Compose (if you need Compose-specific integration like viewModel, rememberKoinInject)
    implementation(libs.koin.androidx.compose)

    // Ktor Client Core
    implementation(libs.ktor.client.core)
    // Ktor Client for Android (engine)
    implementation(libs.ktor.client.android)
    // Ktor Client Content Negotiation (for JSON parsing)
    implementation(libs.ktor.client.content.negotiation)
    // Ktor Client JSON Serializer (Kotlinx Serialization)
    implementation(libs.ktor.serialization.kotlinx.json)
    // Kotlinx Serialization runtime
    implementation(libs.kotlinx.serialization.json)
    // Logging for Ktor
    implementation(libs.ktor.client.logging)

    implementation(libs.accompanist.permissions)
}