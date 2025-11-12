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

    // ============================================================
    // 🧱 FUNDAMENTALES DE ANDROID Y KOTLIN
    // ============================================================
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // ============================================================
    // 🔒 SEGURIDAD Y AUTENTICACIÓN LOCAL
    // ============================================================
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ============================================================
    // 🎨 JETPACK COMPOSE
    // ============================================================
    // BOM de Compose (mantiene las versiones compatibles)
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.4")
    implementation(libs.androidx.navigation.compose) // Navegación en Compose

    // ============================================================
    // ☁️ FIREBASE Y SERVICIOS DE GOOGLE
    // ============================================================
    // BOM de Firebase (mantiene todas las versiones compatibles)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Firebase principales
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Conectores base con Google Play Services (💡 estos evitan tu error)
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.android.gms:play-services-tasks:18.1.0")
    implementation("com.google.android.gms:play-services-auth:21.1.0")

    // ============================================================
    // 🧪 TESTING
    // ============================================================
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Compose testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}


