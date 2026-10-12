plugins {
    id("com.android.application")
}

android {
    namespace = "net.sourceforge.opencamera"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.sourceforge.opencamera"
        minSdk = 21
        targetSdk = 34
        versionCode = 28
        versionName = "1.24"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("${rootDir}/AndroidManifest.xml")
            java.directories.add("${rootDir}/src")
            res.directories.add("${rootDir}/res")
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
