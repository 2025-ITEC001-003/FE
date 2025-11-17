package com.example.jesafe.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

/**
 * 사용자의 인증 토큰 등 환경설정 정보를 DataStore에 저장하고 관리하는 클래스입니다.
 *
 * @property context 애플리케이션 컨텍스트
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    /**
     * DataStore에 저장된 액세스 토큰을 Flow 형태로 가져옵니다.
     * 토큰이 없으면 null을 반환합니다.
     */
    val accessToken: Flow<String?> = context.dataStore.data
        .map {
            it[PreferencesKeys.ACCESS_TOKEN]
        }

    /**
     * 서버로부터 발급받은 액세스 토큰을 DataStore에 저장합니다.
     *
     * @param token 저장할 액세스 토큰
     */
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit {
            it[PreferencesKeys.ACCESS_TOKEN] = token
        }
    }

    /**
     * DataStore에 저장된 모든 사용자 정보를 삭제합니다. (로그아웃 시 사용)
     */
    suspend fun clear() {
        context.dataStore.edit {
            it.clear()
        }
    }
}
