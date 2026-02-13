plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

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
    // INTERNAL IDENTITY: Matches your Kotlin files. This FIXES the "Unresolved R" errors.
    namespace = "com.example.emergencycommunicationsystem" 
    compileSdk = 35

    signingConfigs {
        create("debugCustom") {
            storeFile = file("${System.getProperty("user.home")}/.android/ecs-debug.keystore")
            storePassword = "android"
            keyAlias = "ecsdebug"
            keyPassword = "android"
        }
    }

    defaultConfig {
        // EXTERNAL IDENTITY: Matches your Firebase Console. This FIXES the "No Data" issue.
        applicationId = "com.lgu.emergencycommunicationsystem" 
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${getLocalProperty("GOOGLE_WEB_CLIENT_ID", "")}\"")
        buildConfigField("String", "GOOGLE_ANDROID_CLIENT_ID", "\"${getLocalProperty("GOOGLE_ANDROID_CLIENT_ID", "")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${getLocalProperty("GEMINI_API_KEY", "")}\"")
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"${getLocalProperty("OPENWEATHER_API_KEY", "")}\"")
        buildConfigField("String", "LOCAL_DEVICE_HOST", "\"${getLocalProperty("LOCAL_DEVICE_HOST", "192.168.1.9")}\"")
        buildConfigField("boolean", "ALLOW_LOCAL_FALLBACK", "false")
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugCustom")
            buildConfigField("boolean", "ALLOW_LOCAL_FALLBACK", getLocalProperty("ALLOW_LOCAL_FALLBACK", "false"))
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("boolean", "ALLOW_LOCAL_FALLBACK", "false")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation("androidx.compose.material:material-icons-extended:1.5.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-gif:2.4.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.mlkit:translate:17.0.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    implementation("io.github.webrtc-sdk:android:104.5112.09")
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }
}
