plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "eu.wewox.minabox"

    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        applicationId = "eu.wewox.minabox"

        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()

        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.sourceCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.targetCompatibility.get())
    }
    kotlinOptions {
        jvmTarget = libs.versions.java.jvmTarget.get()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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