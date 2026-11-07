plugins {
    alias(libs.plugins.cinelog.android.library)
    alias(libs.plugins.cinelog.android.compose)
}

android {
    namespace = "com.gabriel.cinelog.core.ui"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.designsystem)

    // LazyPagingItems is part of this module's public API (PagingMovieGrid)
    api(libs.androidx.paging.compose)
    implementation(libs.coil.compose)
    // Registers itself automatically: Coil downloads images with OkHttp
    implementation(libs.coil.network.okhttp)
}
