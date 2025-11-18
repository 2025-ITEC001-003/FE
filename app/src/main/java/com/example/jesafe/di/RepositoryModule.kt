package com.example.jesafe.di

import com.example.jesafe.data.repository.AuthRepository
import com.example.jesafe.data.repository.AuthRepositoryImpl
import com.example.jesafe.data.repository.UserRepository
import com.example.jesafe.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * AuthRepository의 구현체로 AuthRepositoryImpl을 바인딩합니다.
     * ViewModel 등에서 AuthRepository를 주입받으면, Hilt가 자동으로 AuthRepositoryImpl 인스턴스를 제공합니다.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
