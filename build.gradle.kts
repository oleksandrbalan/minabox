import com.diffplug.gradle.spotless.SpotlessPlugin
import io.gitlab.arturbosch.detekt.DetektPlugin

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
    alias(libs.plugins.mavenpublish)
    alias(libs.plugins.org.jetbrains.kotlin.jvm) apply false
}

configure(subprojects) {
    apply<DetektPlugin>()
    apply<SpotlessPlugin>()

    spotless {
        kotlin {
            target("**/*.kt")
            ktlint("0.43.2")
        }
    }
}
