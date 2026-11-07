plugins {
    alias(libs.plugins.cinelog.android.feature)
}

android {
    namespace = "com.gabriel.cinelog.feature.details.impl"
}

dependencies {
    implementation(projects.feature.details.api)
}
