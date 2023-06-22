plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.android.library)
}

kotlin {
	android()
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":minabox"))
			}
		}

		val jvmMain by getting

		val androidMain by getting {
			dependencies {
				implementation(libs.androidx.graphics.shapes)
				implementation(libs.compose.uitooling)
				implementation(libs.compose.uitoolingpreview)
			}
		}
	}
}

android {
	namespace = "eu.wewox.minabox.demo"

	compileSdk = libs.versions.sdk.compile.get().toInt()

	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.java.sourceCompatibility.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.java.targetCompatibility.get())
	}
}
