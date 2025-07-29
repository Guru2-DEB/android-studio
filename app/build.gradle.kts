import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Room @Entity/@Dao 어노테이션 프로세싱 활성화를 위한 KAPT
    id("org.jetbrains.kotlin.kapt")
    // 또는, 버전 카탈로그에 정의돼 있으면:
    // alias(libs.plugins.kotlin.kapt)
}

val localProperties = Properties().apply {
    load(File(rootDir, "local.properties").inputStream())
}

android {
    namespace = "com.example.deb"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.deb"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val clientId     = localProperties["NAVER_CLIENT_ID"] as String
        val clientSecret = localProperties["NAVER_CLIENT_SECRET"] as String

        buildConfigField("String", "NAVER_CLIENT_ID",     "\"$clientId\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$clientSecret\"")
    }

    buildFeatures {
        buildConfig = true
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
}

dependencies {
    // Android X
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)

    // Retrofit + Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Volley for HTTP
    implementation("com.android.volley:volley:1.2.1")

    implementation("androidx.fragment:fragment-ktx:1.6.1")

    // --- Room (SQLite) ---
    implementation("androidx.room:room-runtime:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")

    // --- Coroutines (for async / Room) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
