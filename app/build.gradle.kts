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
        versionCode = 12
        versionName = "1.5.4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("NOCHOICE_KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("NOCHOICE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("NOCHOICE_KEY_ALIAS")
                keyPassword = System.getenv("NOCHOICE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    implementation("com.google.zxing:core:3.5.4")
}
