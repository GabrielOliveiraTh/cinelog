plugins {
    alias(libs.plugins.cinelog.android.feature)
}

android {
    namespace = "com.gabriel.cinelog.feature.discover.impl"
}

dependencies {
    implementation(projects.feature.discover.api)
}
