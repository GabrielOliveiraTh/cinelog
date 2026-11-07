plugins {
    alias(libs.plugins.cinelog.android.feature)
}

android {
    namespace = "com.gabriel.cinelog.feature.search.impl"
}

dependencies {
    implementation(projects.feature.search.api)
}
