plugins {
    alias(libs.plugins.cinelog.android.feature)
}

android {
    namespace = "com.gabriel.cinelog.feature.watchlist.impl"
}

dependencies {
    implementation(projects.feature.watchlist.api)
}
