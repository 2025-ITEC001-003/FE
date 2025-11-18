package com.example.jesafe.data.repository

import android.util.Log
import com.example.jesafe.data.model.AuthRequest
import com.example.jesafe.data.model.AuthResponse
import com.example.jesafe.data.model.UpdateUserRequest
import com.example.jesafe.data.remote.AuthApiService
import com.kakao.sdk.user.UserApiClient
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * AuthRepository의 구현체입니다.
 * AuthApiService를 사용하여 원격 서버와 통신합니다.
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {

    /**
     * 카카오 로그인을 서버에 요청합니다.
     */
    override suspend fun requestKakaoLogin(authRequest: AuthRequest): Response<AuthResponse> {
        return authApiService.kakaoLogin(authRequest)
    }

    /**
     * 사용자의 추가 정보를 서버에 업데이트합니다.
     * 토큰 앞에 "Bearer "를 붙여 헤더 형식에 맞춥니다.
     */
    override suspend fun updateUserInfo(token: String, request: UpdateUserRequest): Response<Unit> {
        return authApiService.updateUserInfo("Bearer $token", request)
    }

    override suspend fun logout() {
        suspendCoroutine<Unit> { continuation ->
            UserApiClient.instance.unlink { error ->
                if (error != null) {
                    Log.e("AuthRepositoryImpl", "카카오 연결 끊기 실패", error)
                } else {
                    Log.d("AuthRepositoryImpl", "카카오 연결 끊기 성공")
                }
                continuation.resume(Unit)
            }
        }
    }
}
