package com.gabriel.cinelog.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun Project.configureKotlinAndroid(extension: CommonExtension) {
    extension.apply {
        compileSdk = AndroidConfig.COMPILE_SDK
        defaultConfig.minSdk = AndroidConfig.MIN_SDK

        compileOptions.sourceCompatibility = AndroidConfig.JAVA_VERSION
        compileOptions.targetCompatibility = AndroidConfig.JAVA_VERSION

        buildFeatures.aidl = false
        buildFeatures.buildConfig = false
        buildFeatures.shaders = false

        // Dependency updates are proposed by Dependabot (.github/dependabot.yml), not by lint warnings
        lint.disable += setOf("NewerVersionAvailable", "GradleDependency", "AndroidGradlePluginVersion")
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(AndroidConfig.JVM_TARGET)
    }
}
