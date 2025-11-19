package com.example.jesafe.ui.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.datastore.UserPreferencesRepository
import com.example.jesafe.data.model.AuthRequest
import com.example.jesafe.data.repository.AuthRepository
import com.example.jesafe.data.repository.UserRepository
import com.example.jesafe.model.User
import com.example.jesafe.state.LoadingState
import com.kakao.sdk.auth.AuthCodeClient
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
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
    data class ExistingUser(val user: User) : LoginState()
    // 신규 사용자로 로그인 성공
    data class NewUser(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
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
            val user = userRepository.getUser().first()
            if (!token.isNullOrBlank() && user != null) {
                _loginState.value = LoginState.ExistingUser(user)
            }
        }
    }

    /**
     * 카카오 로그인을 시작하고 인증 코드를 요청합니다.
     *
     * 인증 코드를 성공적으로 받으면 `requestKakaoLoginWithAuthCode`를 호출하여 서버에 로그인을 요청합니다.
     *
     * @param context 카카오 SDK가 사용할 컨텍스트.
     */
    fun startKakaoLogin(context: Context) {
        val authCodeCallback: (String?, Throwable?) -> Unit = { code, error ->
            if (error != null) {
                Log.e("KAKAO_LOGIN", "인가 코드 발급 실패", error)
                _loginState.value = LoginState.Error("로그인에 실패했습니다: ${error.message}")
            } else if (code != null) {
                Log.i("KAKAO_LOGIN", "인가 코드 발급 성공: $code")
                requestKakaoLoginWithAuthCode(code)
            }
        }

        if (AuthCodeClient.instance.isKakaoTalkLoginAvailable(context)) {
            AuthCodeClient.instance.authorizeWithKakaoTalk(context) { code, error ->
                if (error != null) {
                    Log.e("KAKAO_LOGIN", "카카오톡으로 인가 코드 받기 실패", error)

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@authorizeWithKakaoTalk
                    }

                    AuthCodeClient.instance.authorizeWithKakaoAccount(
                        context,
                        callback = authCodeCallback
                    )
                } else if (code != null) {
                    authCodeCallback(code, null)
                }
            }
        } else {
            AuthCodeClient.instance.authorizeWithKakaoAccount(context, callback = authCodeCallback)
        }
    }


    fun requestKakaoLoginWithAuthCode(authCode: String) {
        Log.d("LOGIN_API_TEST", "[Request] 서버로 전송할 인가 코드: $authCode")

        viewModelScope.launch {
            LoadingState.show()
            try {
                val response = authRepository.requestKakaoLogin(AuthRequest(authCode))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d("LOGIN_API_TEST", "[Response Body] 서버 응답 전체: $body")
                    userPreferencesRepository.saveAccessToken(body.accessToken)

                    if (body.isNewUser) {
                        val newUser = User(
                            id = body.id,
                            nickname = body.nickname ?: "사용자",
                            birthYear = null,
                            gender = null,
                            diseaseInfo = null,
                            phone = null,
                            email = null
                        )
                        userRepository.saveUser(newUser)
                        _loginState.value = LoginState.NewUser(newUser)
                    } else {
                        val existingUser = User(
                            id = body.id,
                            nickname = body.nickname ?: "사용자",
                            birthYear = body.birthYear,
                            gender = body.gender,
                            diseaseInfo = body.diseaseInfo,
                            phone = body.phone,
                            email = body.email
                        )
                        userRepository.saveUser(existingUser)
                        _loginState.value = LoginState.ExistingUser(existingUser)
                    }

                } else {
                    val errorBody = response.errorBody()?.string() ?: "알 수 없는 오류"
                    Log.e("LOGIN_API_TEST", "[Response Error] 로그인 실패: ${response.code()} / $errorBody")
                    _loginState.value = LoginState.Error("로그인 실패: ${response.code()} / $errorBody")
                }
            } catch (e: Exception) {
                Log.e("LOGIN_API_TEST", "[Network Error] 로그인 실패: ${e.message}", e)
                _loginState.value = LoginState.Error("로그인 실패 (네트워크): ${e.message}")
            } finally {
                LoadingState.hide()
            }
        }
    }

    suspend fun logout() {
        LoadingState.show()
        try {
            authRepository.logout()
            userPreferencesRepository.clearAccessToken()
            _loginState.value = LoginState.Initial
        } finally {
            LoadingState.hide()
        }
    }
}
