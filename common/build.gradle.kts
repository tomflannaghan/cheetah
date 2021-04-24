import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "0.3.1"
    id("com.android.library")
    id("kotlin-android-extensions")
    id("com.squareup.sqldelight")
}

group = "com.flannaghan.cheetah"
version = "1.0"

repositories {
    google()
}

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                implementation("com.squareup.sqldelight:runtime:1.4.4")
                implementation("com.squareup.sqldelight:coroutines-extensions:1.4.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.appcompat:appcompat:1.3.0-rc01")
                api("androidx.core:core-ktx:1.3.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3")
                implementation("androidx.compose.runtime:runtime-livedata:1.0.0-beta04")
                implementation("com.squareup.sqldelight:android-driver:1.4.4")

            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:sqlite-driver:1.4.4")
                implementation("org.xerial:sqlite-jdbc:3.30.1")
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
            }
        }
    }
}

android {
    compileSdkVersion(29)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(29)
    }
}

sqldelight {
    database("WordDatabase") {
        packageName = "com.flannaghan.cheetah.common.db"
    }
}