package com.example.jesafe.di

import android.content.Context
import androidx.room.Room
import com.example.jesafe.data.local.AccidentDao
import com.example.jesafe.data.local.AppDatabase
import com.example.jesafe.data.repository.AccidentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "accident_db"
        ).build()

    @Provides
    fun provideAccidentDao(db: AppDatabase): AccidentDao = db.accidentDao()

    @Provides
    @Singleton
    fun provideAccidentRepository(dao: AccidentDao): AccidentRepository =
        AccidentRepository(dao)
}