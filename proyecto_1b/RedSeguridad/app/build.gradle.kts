// app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace  = "com.example.redseguridad"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.redseguridad"
        minSdk        = 26
        targetSdk     = 36
        versionCode   = 1
        versionName   = "1.0"

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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // ── FASE 1: Habilitar ViewBinding ──────────────────────────────────────
    // Permite acceder a las vistas sin findViewById ni synthetics.
    // Genera una clase Binding por cada archivo XML de layout.
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // ── Existentes (generadas por Android Studio) ──────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ── FASE 1: Arquitectura MVVM ──────────────────────────────────────────
    // Coroutines: Dispatchers.IO para red y DataStore, viewModelScope
    implementation(libs.kotlinx.coroutines.android)

    // ViewModel + StateFlow: ciclo de vida correcto, no re-crea en rotación
    implementation(libs.lifecycle.viewmodel.ktx)

    // repeatOnLifecycle: colectar StateFlow de forma segura desde Fragment
    implementation(libs.lifecycle.runtime.ktx)

    // by viewModels(): delegado de Kotlin para obtener ViewModel en Fragment
    implementation(libs.androidx.fragment.ktx)

    // ── FASE 1: Persistencia segura (Módulo 3) ─────────────────────────────
    // Jetpack DataStore Preferences: reemplaza SharedPreferences de forma reactiva
    implementation(libs.datastore.preferences)

    // EncryptedSharedPreferences: cifrado AES-256-SIV (llaves) + AES-128-GCM (valores)
    implementation(libs.security.crypto)

    // ── Test ───────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}