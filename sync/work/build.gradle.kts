plugins {
    alias(libs.plugins.cinelog.android.library)
    alias(libs.plugins.cinelog.hilt)
}

android {
    namespace = "com.gabriel.cinelog.sync.work"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.feature.details.api)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    // Generates the factories for @HiltWorker classes
    ksp(libs.androidx.hilt.compiler)

    testImplementation(projects.core.testing)
}
