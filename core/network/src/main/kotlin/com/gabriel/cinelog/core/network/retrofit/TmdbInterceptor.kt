package com.gabriel.cinelog.core.network.retrofit

import okhttp3.Interceptor
import okhttp3.Response

internal class TmdbInterceptor(private val accessToken: String, private val language: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.newBuilder()
            .addQueryParameter("language", language)
            .build()

        return chain.proceed(
            request.newBuilder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/json")
                .build(),
        )
    }
}
