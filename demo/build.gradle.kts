import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.cocoapods)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.android.library)
    id("convention.jvm.toolchain")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.default()

    androidTarget()

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "Demo Compose Multiplatform module"
        homepage = "---"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosdemo/Podfile")
        framework {
            baseName = "demo"
            isStatic = true
        }
        extraSpecAttributes["resources"] =
            "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":minabox"))
                implementation(compose.material3)
            }
        }

        all {
            languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        }
    }
}

android {
    namespace = "eu.wewox.minabox.demo"

    compileSdk = libs.versions.sdk.compile.get().toInt()
}
