package com.gabriel.cinelog.convention

import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal fun Project.configureDetekt() {
    pluginManager.apply("dev.detekt")
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig.set(true)
        parallel.set(true)
        config.setFrom(layout.settingsDirectory.file("config/detekt/detekt.yml"))
        basePath.set(layout.settingsDirectory)
    }
}
