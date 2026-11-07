import java.util.Properties

plugins {
    alias(libs.plugins.cinelog.android.library)
    alias(libs.plugins.cinelog.hilt)
    alias(libs.plugins.kotlin.serialization)
}

// The TMDB token lives in local.properties (git-ignored) and is exposed as BuildConfig.TMDB_ACCESS_TOKEN.
val tmdbAccessToken: String = providers
    .fileContents(layout.settingsDirectory.file("local.properties"))
    .asText
    .map { text -> Properties().apply { load(text.reader()) }.getProperty("tmdb.accessToken", "") }
    .getOrElse("")

android {
    namespace = "com.gabriel.cinelog.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "TMDB_ACCESS_TOKEN", "\"$tmdbAccessToken\"")
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)

    testImplementation(libs.junit)
}
