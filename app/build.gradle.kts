plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.myapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Android 基本ライブラリ
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // CameraX：カメラの基本機能
    implementation("androidx.camera:camera-core:1.4.1")

    // CameraX：カメラとの接続
    implementation("androidx.camera:camera-camera2:1.4.1")

    // CameraX：画面のライフサイクルとの連携
    implementation("androidx.camera:camera-lifecycle:1.4.1")

    // CameraX：カメラ映像のプレビュー表示
    implementation("androidx.camera:camera-view:1.4.1")

    // テスト用ライブラリ
    testImplementation("junit:junit:4.13.2")
}