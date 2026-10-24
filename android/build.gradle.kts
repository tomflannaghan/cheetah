plugins {
    id("com.android.application")
    kotlin("plugin.compose")
}

group = "com.flannaghan.cheetah"
version = "1.0"

dependencies {
    implementation(project(":common"))
    implementation("androidx.activity:activity-compose:1.13.0")
}

android {
    namespace = "com.flannaghan.cheetah.android"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.flannaghan.cheetah.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
