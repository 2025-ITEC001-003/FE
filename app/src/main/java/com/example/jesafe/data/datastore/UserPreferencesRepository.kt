package com.example.jesafe.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.jesafe.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val USER_DATA = stringPreferencesKey("user_data")
    }

    val accessToken: Flow<String?> = context.dataStore.data
        .map {
            it[PreferencesKeys.ACCESS_TOKEN]
        }

    val user: Flow<User?> = context.dataStore.data
        .map {
            val json = it[PreferencesKeys.USER_DATA]
            Log.d("UserRepo", "Loaded user JSON: $json")
            if (json != null) {
                try {
                    Json.decodeFromString<User>(json)
                } catch (e: Exception) {
                    Log.e("UserRepo", "Failed to decode user JSON", e)
                    null // 역직렬화 실패 시 null 반환
                }
            } else {
                null
            }
        }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit {
            it[PreferencesKeys.ACCESS_TOKEN] = token
        }
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit {
            val json = Json.encodeToString(user)
            Log.d("UserRepo", "Saving user JSON: $json")
            it[PreferencesKeys.USER_DATA] = json
        }
    }

    /**
     * 액세스 토큰만 삭제합니다. (로그아웃 시 사용)
     * 사용자 정보(닉네임, 기저질환 등)는 유지됩니다.
     */
    suspend fun clearAccessToken() {
        context.dataStore.edit {
            it.remove(PreferencesKeys.ACCESS_TOKEN)
        }
    }

    /**
     * DataStore에 저장된 모든 사용자 정보를 삭제합니다.
     */
    suspend fun clearAll() {
        context.dataStore.edit {
            it.clear()
        }
    }
}
