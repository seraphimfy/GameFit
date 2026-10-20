// Плагины Android-приложения, Kotlin, сериализации и генерации Room-кода.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.kapt")
}

android {
    // Идентификатор пакета и версия Android SDK приложения.
    namespace = "com.gamefit.android"
    compileSdk = 34

    defaultConfig {
        // Настройки APK: идентификатор, минимальная и целевая версии Android.
        applicationId = "com.gamefit.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        // Конфигурация release-сборки.
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        // Исходный и целевой уровень Java для Android-кода.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        // JVM-цель Kotlin должна совпадать с Java-настройками.
        jvmTarget = "17"
    }

    buildFeatures {
        // View Binding создаёт типобезопасный доступ к XML-разметке.
        viewBinding = true
    }
}

dependencies {
    // Базовые Android-компоненты интерфейса.
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lifecycle scope и корутины для фоновых операций.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room отвечает за локальное SQLite-хранилище приложения.
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Ktor и Kotlin Serialization используются для импорта матчей из OpenDota.
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
