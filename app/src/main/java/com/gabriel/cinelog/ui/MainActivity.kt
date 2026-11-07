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

package com.gabriel.cinelog.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gabriel.cinelog.core.designsystem.CinelogTheme
import com.gabriel.cinelog.feature.details.navigation.MovieDetailsDeepLink
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingDeepLinkMovieId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)
        // After a rotation the intent is the same, but the details screen is already in the
        // restored back stack: only read the link on the first creation
        if (savedInstanceState == null) {
            pendingDeepLinkMovieId = MovieDetailsDeepLink.movieIdFrom(intent?.data)
        }
        setContent {
            CinelogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainNavigation(
                        deepLinkMovieId = pendingDeepLinkMovieId,
                        onDeepLinkConsume = { pendingDeepLinkMovieId = null },
                    )
                }
            }
        }
    }

    // singleTop: a notification tapped while the app is open lands here
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        MovieDetailsDeepLink.movieIdFrom(intent.data)?.let { pendingDeepLinkMovieId = it }
    }
}
