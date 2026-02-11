plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Required for Room
}

// Load local.properties file
fun getLocalProperty(key: String, defaultValue: String): String {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.readLines().forEach { line ->
            if (line.startsWith("$key=")) {
                return line.substringAfter("=").trim()
            }
        }
    }
    return defaultValue
}

android {
    namespace = "com.example.emergencycommunicationsystem"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.emergencycommunicationsystem"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Google OAuth Credentials from local.properties (NOT hardcoded)
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${getLocalProperty("GOOGLE_WEB_CLIENT_ID", "YOUR_CLIENT_ID_PLACEHOLDER")}\""
        )
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_SECRET",
            "\"${getLocalProperty("GOOGLE_WEB_CLIENT_SECRET", "YOUR_SECRET_PLACEHOLDER")}\""
        )
        buildConfigField(
            "String",
            "GOOGLE_ANDROID_CLIENT_ID",
            "\"${getLocalProperty("GOOGLE_ANDROID_CLIENT_ID", "YOUR_ANDROID_ID_PLACEHOLDER")}\""
        )
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${getLocalProperty("GEMINI_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "OPENWEATHER_API_KEY",
            "\"${getLocalProperty("OPENWEATHER_API_KEY", "YOUR_OPENWEATHER_API_KEY_PLACEHOLDER")}\""
        )
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
    
    // Configure for 16 KB page size compatibility (Android 15+ requirement)
    packaging {
        jniLibs {
            useLegacyPackaging = false
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
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Compose Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.5.1")
    
    // Coil for Compose
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-gif:2.4.0")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Logging Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Google Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    
    // Google ML Kit Translation
    implementation("com.google.mlkit:translate:17.0.2")
    
    // Compose ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // OSMDroid for OpenStreetMap (Using this instead of Tangram)
    implementation("org.osmdroid:osmdroid-android:6.1.14")

    // WebRTC - Official and stable version
    implementation("io.github.webrtc-sdk:android:104.5112.09")
    
    // Socket.IO - Official client
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

    // Testing Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
