plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.bambuqr"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.bambuqr"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
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
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // --- TAMBAHKAN 3 LIBRARY INI UNTUK PANDA QR ---
    // 1. Ikon Material lengkap (Wi-Fi, WhatsApp, Warna, Share, dll.)
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // 2. ZXing Core untuk Generate QR Code Custom Warna (100% Offline & Ringan)
    implementation("com.google.zxing:core:3.5.3")

    // 3. Google Code Scanner (Scan QR instan resmi dari Google, sangat aman untuk Play Store)
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
}