package com.example.jesafe.di

import com.example.jesafe.BuildConfig
import com.example.jesafe.data.remote.AuthApiService
import com.example.jesafe.data.network.ChatApiService
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DefaultOkHttpClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class StreamingOkHttpClient

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @DefaultOkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @StreamingOkHttpClient
    @Provides
    @Singleton
    fun provideStreamingOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MINUTES) // 스트리밍을 위해 타임아웃 제거
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(@DefaultOkHttpClient okHttpClient: OkHttpClient): Retrofit {
        val baseUrl = if (BuildConfig.SERVER_URL.isNotBlank()) {
            BuildConfig.SERVER_URL
        } else {
            "http://10.0.2.2:8000/"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApiService(@StreamingOkHttpClient streamingOkHttpClient: OkHttpClient): ChatApiService {
        val streamingRetrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .client(streamingOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
        return streamingRetrofit.create(ChatApiService::class.java)
    }
}
