plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.mavenpublish)
}

kotlin {
	androidTarget()
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(compose.runtime)
				api(compose.foundation)
			}
		}

		val jvmMain by getting

		val androidMain by getting

		all {
			languageSettings.optIn("androidx.compose.foundation.ExperimentalFoundationApi")
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
	kotlin {
		explicitApi()

		androidTarget {
			publishLibraryVariants("release", "debug")
		}
	}
	composeOptions {
		kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
	}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = libs.versions.java.jvmTarget.get()
	}
}
