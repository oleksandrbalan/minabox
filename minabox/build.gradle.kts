plugins {
	kotlin("multiplatform")
	id("org.jetbrains.compose")
	id("com.android.library")
	alias(libs.plugins.mavenpublish)
}

kotlin {
	android()
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(compose.runtime)
				api(compose.foundation)
				api(compose.material3)
			}
		}

		val jvmMain by getting

		val androidMain by getting {
			dependencies {
				/*implementation(platform(libs.compose.bom))
				implementation(libs.compose.foundation)
				implementation(libs.compose.ui)*/
			}
		}
	}
}

android {
	namespace = "eu.wewox.minabox"

	compileSdk = libs.versions.sdk.compile.get().toInt()

	defaultConfig {
		minSdk = libs.versions.sdk.min.get().toInt()
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
