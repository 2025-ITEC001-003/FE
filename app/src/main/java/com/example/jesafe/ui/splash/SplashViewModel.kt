package com.example.jesafe.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 앱의 인증 상태를 확인하고 초기 라우팅을 결정하는 ViewModel입니다.
 *
 * @property userPreferencesRepository 토큰을 관리하는 DataStore 리포지토리
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    /**
     * DataStore에 저장된 액세스 토큰의 유무를 확인하여 인증 상태를 결정합니다.
     */
    private fun checkAuthState() {
        viewModelScope.launch {
            // .first()를 사용하여 Flow에서 첫 번째 값을 한 번만 가져옵니다.
            val accessToken = userPreferencesRepository.accessToken.first()
            if (accessToken.isNullOrBlank()) {
                _authState.value = AuthState.Unauthenticated
            } else {
                _authState.value = AuthState.Authenticated
            }
        }
    }
}

/**
 * 앱의 초기 인증 상태를 나타내는 Sealed Class 입니다.
 */
sealed class AuthState {
    /** 로딩 중 */
    object Loading : AuthState()
    /** 인증 완료 (토큰 존재) */
    object Authenticated : AuthState()
    /** 미인증 (토큰 없음) */
    object Unauthenticated : AuthState()
}
