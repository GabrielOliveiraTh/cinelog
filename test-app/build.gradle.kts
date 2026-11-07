/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.gabriel.cinelog.test.navigation"
    compileSdk = 37
    targetProjectPath = ":app"

    defaultConfig {
        minSdk = 24
        targetSdk = 37

        testInstrumentationRunner = "com.gabriel.cinelog.core.testing.HiltTestRunner"
    }

    buildFeatures {
        aidl = false
        buildConfig = false
        shaders = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(projects.app)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(projects.core.data)
    implementation(projects.core.testing)
    implementation(projects.core.model)
    // The app graph injects HiltWorkerFactory (Cinelog.kt), so Hilt needs it here too
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.work.runtime)

    implementation(libs.androidx.test.core)

    implementation(libs.hilt.android.testing)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.compose.ui.test.junit4)
}
