plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())

    android {
        namespace = "com.flannaghan.cheetah.common"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    jvm("desktop") {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
            // The tests read the real word lists and databases.
            systemProperty("cheetah.dataDir", rootProject.file("data").absolutePath)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.compose.runtime)
                api(libs.compose.foundation)
                api(libs.compose.material)
                api(libs.lifecycle.viewmodel.compose)
                implementation(libs.lifecycle.runtime.compose)
                implementation(libs.compose.material.icons.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.sqldelight.runtime)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.sqldelight.android.driver)
            }
        }
        getByName("desktopMain") {
            dependencies {
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
        getByName("desktopTest") {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation(libs.kotlinx.coroutines.test)
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
