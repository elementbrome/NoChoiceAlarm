plugins {
    id("com.android.application")
}

android {
    namespace = "com.qralarm.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.qralarm.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.2.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("com.google.zxing:core:3.5.4")
}
