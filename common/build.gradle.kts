import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.kotlin.multiplatform.library")
    id("app.cash.sqldelight")
}

group = "com.flannaghan.cheetah"
version = "1.0"

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.flannaghan.cheetah.common"
        compileSdk = 37
        minSdk = 24
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                api("org.jetbrains.compose.runtime:runtime:1.12.2")
                api("org.jetbrains.compose.foundation:foundation:1.12.2")
                api("org.jetbrains.compose.material:material:1.12.2")
                implementation("org.jetbrains.compose.material:material-icons-core:1.7.3")
                implementation("app.cash.sqldelight:runtime:2.4.1")
                implementation("app.cash.sqldelight:coroutines-extensions:2.4.1")
                implementation("com.google.code.gson:gson:2.8.6")
            }
        }
        androidMain {
            dependencies {
                api("androidx.appcompat:appcompat:1.8.0")
                api("androidx.core:core-ktx:1.19.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
                implementation("androidx.compose.runtime:runtime-livedata:1.12.1")
                implementation("app.cash.sqldelight:android-driver:2.4.1")
            }
        }
        getByName("desktopMain") {
            dependencies {
                implementation("app.cash.sqldelight:sqlite-driver:2.4.1")
            }
        }
        getByName("desktopTest") {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
    }
}

sqldelight {
    databases {
        create("WordDatabase") {
            packageName.set("com.flannaghan.cheetah.common.db")
        }
    }
}
