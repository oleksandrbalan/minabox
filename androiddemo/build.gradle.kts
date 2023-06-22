@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "eu.wewox.minabox"
    compileSdk = 33

    defaultConfig {
        applicationId = "eu.wewox.minabox"
        minSdk = 22
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.sourceCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.targetCompatibility.get())
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    implementation(project(":minabox"))
    implementation(project(":demo"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.uitooling)
    implementation(libs.compose.uitoolingpreview)
    implementation(libs.androidx.activitycompose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.graphics.shapes)
}