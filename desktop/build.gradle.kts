import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())

    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += "-Dcheetah.dataDir=${rootProject.file("data").absolutePath}"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Cheetah"
            packageVersion = "1.0.0"
        }
    }
}
