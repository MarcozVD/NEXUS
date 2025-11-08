plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.Marcos.nexus"
    compileSdk = 36
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // <-- ¡VERSIÓN CORREGIDA!
    }

    defaultConfig {
        applicationId = "com.Marcos.nexus"
        minSdk = 24
        targetSdk = 36
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
    }
}

    // En tu archivo build.gradle o build.gradle.kts (del módulo 'app')

dependencies {
    // --- LIBRERÍAS DE ANDROIDX UNIFICADAS ---
    // (Asegúrate de que no haya duplicados de estas en tu archivo)
    implementation("androidx.core:core-ktx:1.12.0") // O la versión que prefieras, pero solo una
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3") // O la que necesites
    implementation("androidx.activity:activity-compose:1.9.0") // Usa la versión más reciente
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    // --- BOM DE COMPOSE ---
    // El BOM (Bill of Materials) gestiona las versiones de las librerías de Compose para que sean compatibles
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // --- BOM DE FIREBASE ¡CORREGIDO! ---
    // Unificamos a la versión más reciente y usamos las dependencias principales
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // <-- SOLO UNA LÍNEA DE BOM
    implementation("com.google.firebase:firebase-auth")          // <-- SIN '-ktx'
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.androidx.navigation.compose)    // <-- SIN '-ktx'

    // --- DEPENDENCIAS DE TEST ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

