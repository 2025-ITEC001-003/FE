package com.example.jesafe.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.datastore.UserPreferencesRepository
import com.example.jesafe.data.model.AuthRequest
import com.example.jesafe.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 로그인 과정의 UI 상태를 나타내는 Sealed Class입니다.
 */
sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    // 기존 사용자로 로그인 성공
    data class ExistingUser(val serverAccessToken: String) : LoginState()
    // 신규 사용자로 로그인 성공
    data class NewUser(val serverAccessToken: String, val nickname: String?) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val token = userPreferencesRepository.accessToken.first()
            if (!token.isNullOrBlank()) {
                _loginState.value = LoginState.ExistingUser(token)
            }
        }
    }

    fun requestKakaoLoginWithAuthCode(authCode: String) {
        Log.d("LOGIN_API_TEST", "[Request] 서버로 전송할 인가 코드: $authCode")

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = authRepository.requestKakaoLogin(AuthRequest(authCode))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d("LOGIN_API_TEST", "[Response Body] 서버 응답 전체: $body") // 서버 응답 전체 로그 추가
                    val serverToken = body.accessToken
                    userPreferencesRepository.saveAccessToken(serverToken)

                    // 서버 응답에 따라 상태 분기
                    if (body.isNewUser) {
                        _loginState.value = LoginState.NewUser(serverToken, body.nickname)
                    } else {
                        _loginState.value = LoginState.ExistingUser(serverToken)
                    }

                } else {
                    val errorBody = response.errorBody()?.string() ?: "알 수 없는 오류"
                    Log.e("LOGIN_API_TEST", "[Response Error] 로그인 실패: ${response.code()} / $errorBody")
                    _loginState.value = LoginState.Error("로그인 실패: ${response.code()} / $errorBody")
                }
            } catch (e: Exception) {
                Log.e("LOGIN_API_TEST", "[Network Error] 로그인 실패: ${e.message}", e)
                _loginState.value = LoginState.Error("로그인 실패 (네트워크): ${e.message}")
            }
        }
    }

    fun performFakeLogin() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val fakeToken = "fake-jwt-token-for-testing"
            userPreferencesRepository.saveAccessToken(fakeToken)
            _loginState.value = LoginState.ExistingUser(fakeToken)
        }
    }

    suspend fun logout() {
        authRepository.logout()
        userPreferencesRepository.clear()
        _loginState.value = LoginState.Initial
    }
}
