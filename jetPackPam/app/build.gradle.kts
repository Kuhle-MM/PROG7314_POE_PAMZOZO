import org.gradle.kotlin.dsl.androidTestImplementation
import org.gradle.kotlin.dsl.debugImplementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "student.projects.jetpackpam"
    compileSdk = 36

    defaultConfig {
        applicationId = "student.projects.jetpackpam"
        minSdk = 29
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

dependencies {
    // --- OneSignal Push Notifications ---
    implementation("com.onesignal:OneSignal:[5.4.0, 5.4.99]") // latest stable version

    // --- Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    //implementation(libs.ads.mobile.sdk)
    implementation(libs.androidx.compose.material)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation(libs.material3.adaptive)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // --- Firebase (BOM-managed) ---
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-inappmessaging-display-ktx")

    // --- Google Sign-In & Credentials API ---
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // --- Image Loading ---
    implementation(libs.coil.compose)

    // --- Networking ---
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.firebase.database)
    implementation(libs.androidx.foundation)
    implementation(libs.material3)
    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.androidx.room.ktx)

    // --- Navigation ---
    implementation("androidx.navigation:navigation-compose:2.7.3")
    implementation("androidx.activity:activity-compose:1.8.2")

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    //Nav Graph
    implementation("androidx.navigation:navigation-compose:2.7.3")
    implementation("androidx.activity:activity-compose:1.8.2")

    // --- Debug ---
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    kapt("androidx.room:room-compiler:2.8.1")


}

// Apply Google Services plugin
apply(plugin = "com.google.gms.google-services")
