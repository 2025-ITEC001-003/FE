package com.example.jesafe.data.repository

import com.example.jesafe.data.datastore.UserPreferencesRepository
import com.example.jesafe.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : UserRepository {
    override fun getUser(): Flow<User?> = userPreferencesRepository.user

    override suspend fun saveUser(user: User) {
        userPreferencesRepository.saveUser(user)
    }
}
