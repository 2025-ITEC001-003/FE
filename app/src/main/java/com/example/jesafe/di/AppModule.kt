package com.example.jesafe.di

import android.content.Context
import androidx.room.Room
import com.example.jesafe.data.local.AccidentDao
import com.example.jesafe.data.local.AppDatabase
import com.example.jesafe.data.local.ChatDao
import com.example.jesafe.data.repository.AccidentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 애플리케이션 전역에서 사용되는 의존성을 제공하는 Hilt 모듈
 *
 * 데이터베이스, 저장소 등 네트워크를 제외한 싱글톤 인스턴스를 제공합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
            "jesafe_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()

    /**
     * 사고 데이터 접근 객체(DAO)를 제공합니다.
     *
     * @param db 데이터베이스 인스턴스
     * @return [AccidentDao] 인스턴스
     */
    @Provides
    fun provideAccidentDao(db: AppDatabase): AccidentDao = db.accidentDao()

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

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
