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

plugins {
    alias(libs.plugins.cinelog.android.library)
    alias(libs.plugins.cinelog.android.room)
    alias(libs.plugins.cinelog.hilt)
}

android {
    namespace = "com.gabriel.cinelog.core.database"

    defaultConfig {
        testInstrumentationRunner = "com.gabriel.cinelog.core.testing.HiltTestRunner"
    }
}

dependencies {
    api(projects.core.model)
    // PagingSource is part of this module's public API (PopularMovieDao)
    api(libs.androidx.paging.common)
    implementation(libs.androidx.room.paging)
    // TypeConverters for list columns
    implementation(libs.kotlinx.serialization.json)
}
