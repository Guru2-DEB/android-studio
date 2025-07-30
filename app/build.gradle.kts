import java.util.Properties
import java.io.FileInputStream
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Room @Entity/@Dao 어노테이션 프로세싱 활성화를 위한 KAPT
    id("org.jetbrains.kotlin.kapt")
}

val localProperties = Properties().apply {
    val file = File(rootDir, "local.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
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

        val clientId = System.getenv("NAVER_CLIENT_ID") ?: localProperties.getProperty("NAVER_CLIENT_ID", "")
        val clientSecret = System.getenv("NAVER_CLIENT_SECRET") ?: localProperties.getProperty("NAVER_CLIENT_SECRET", "")

        buildConfigField("String", "NAVER_CLIENT_ID", "\"$clientId\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$clientSecret\"")
    }

    signingConfigs {
        create("release") {
            storeFile = File(project.projectDir, "release-keystore.jks")
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("ANDROID_KEY_ALIAS") ?: ""
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
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
