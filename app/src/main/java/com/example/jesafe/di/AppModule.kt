package com.example.jesafe.di

import android.content.Context
import androidx.room.Room
import com.example.jesafe.BuildConfig
import com.example.jesafe.data.local.AccidentDao
import com.example.jesafe.data.local.AppDatabase
import com.example.jesafe.data.network.ChatApiService
import com.example.jesafe.data.repository.AccidentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 애플리케이션 전역에서 사용되는 의존성을 제공하는 Hilt 모듈
 *
 * 네트워크 통신, 데이터베이스, 저장소 등의 싱글톤 인스턴스를 제공합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * HTTP 클라이언트를 제공합니다.
     *
     * 타임아웃 설정:
     * - 연결 타임아웃: 30초
     * - 읽기 타임아웃: 120초 (AI 응답 대기 시간)
     * - 쓰기 타임아웃: 30초
     *
     * @return 설정된 [OkHttpClient] 인스턴스
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    /**
     * Retrofit 인스턴스를 제공합니다.
     *
     * Gson을 사용하여 JSON 직렬화/역직렬화를 수행합니다.
     * 서버 URL은 BuildConfig에서 읽어옵니다.
     *
     * @param okHttpClient HTTP 통신에 사용될 클라이언트
     * @return 설정된 [Retrofit] 인스턴스
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /**
     * AI 채팅 API 서비스를 제공합니다.
     *
     * @param retrofit Retrofit 인스턴스
     * @return [ChatApiService] 인터페이스 구현체
     */
    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService =
        retrofit.create(ChatApiService::class.java)

    /**
     * Room 데이터베이스 인스턴스를 제공합니다.
     *
     * fallbackToDestructiveMigration으로 마이그레이션 실패 시 데이터베이스를 재생성합니다.
     *
     * @param context 애플리케이션 컨텍스트
     * @return [AppDatabase] 인스턴스
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "accident_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()

    /**
     * 사고 데이터 접근 객체(DAO)를 제공합니다.
     *
     * @param db 데이터베이스 인스턴스
     * @return [AccidentDao] 인스턴스
     */
    @Provides
    fun provideAccidentDao(db: AppDatabase): AccidentDao = db.accidentDao()

    /**
     * 사고 데이터 저장소를 제공합니다.
     *
     * @param dao 사고 데이터 DAO
     * @return [AccidentRepository] 인스턴스
     */
    @Provides
    @Singleton
    fun provideAccidentRepository(dao: AccidentDao): AccidentRepository =
        AccidentRepository(dao)
}