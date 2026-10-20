plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.planer"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ru.yulia.cloudy"
        minSdk = 26
        targetSdk = 37
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("key_store")
            storePassword = "VsF5f1sliP8Vd1uJiAwD"
            keyAlias = "key_planer"
            keyPassword = "VsF5f1sliP8Vd1uJiAwD"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
            }
        }
    }
    // Две версии для разных магазинов: отличаются только рекламными блоками Яндекса.
    // В Android Studio версия выбирается в панели Build Variants
    // (rustoreDebug / rustoreRelease / huaweiDebug / huaweiRelease).
    flavorDimensions += "store"
    productFlavors {
        create("rustore") {
            dimension = "store"
            buildConfigField("String", "ADS_INTERSTITIAL_ID", "\"R-M-20203104-3\"")
            buildConfigField("String", "ADS_NATIVE_ID", "\"R-M-20203104-2\"")
        }
        create("huawei") {
            dimension = "store"
            buildConfigField("String", "ADS_INTERSTITIAL_ID", "\"R-M-20239167-1\"")
            buildConfigField("String", "ADS_NATIVE_ID", "\"R-M-20239167-2\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.yandex.mobileads)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
