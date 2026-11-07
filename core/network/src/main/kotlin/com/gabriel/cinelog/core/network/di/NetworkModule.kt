package com.gabriel.cinelog.core.network.di

import com.gabriel.cinelog.core.network.BuildConfig
import com.gabriel.cinelog.core.network.CinelogNetworkDataSource
import com.gabriel.cinelog.core.network.NetworkJson
import com.gabriel.cinelog.core.network.retrofit.RetrofitCinelogNetwork
import com.gabriel.cinelog.core.network.retrofit.TmdbApi
import com.gabriel.cinelog.core.network.retrofit.TmdbInterceptor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
private const val TMDB_LANGUAGE = "pt-BR"

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(TmdbInterceptor(BuildConfig.TMDB_ACCESS_TOKEN, TMDB_LANGUAGE))
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
                redactHeader("Authorization")
            },
        )
        .build()

    @Provides
    @Singleton
    fun provideTmdbApi(okHttpClient: OkHttpClient): TmdbApi = Retrofit.Builder()
        .baseUrl(TMDB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(NetworkJson.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()
}

@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkBindsModule {

    @Binds
    fun bindNetworkDataSource(impl: RetrofitCinelogNetwork): CinelogNetworkDataSource
}
