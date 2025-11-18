package com.example.jesafe.data.repository

import com.example.jesafe.data.model.AuthRequest
import com.example.jesafe.data.model.AuthResponse
import com.example.jesafe.data.model.UpdateUserRequest
import retrofit2.Response

/**
 * 인증 및 사용자 정보 관련 데이터 처리를 위한 Repository 인터페이스입니다.
 */
interface AuthRepository {

    /**
     * 카카오 로그인을 통해 서버에 인증을 요청하고, 자체 토큰을 발급받습니다.
     */
    suspend fun requestKakaoLogin(authRequest: AuthRequest): Response<AuthResponse>

    /**
     * 사용자의 추가 정보를 서버에 업데이트합니다.
     */
    suspend fun updateUserInfo(token: String, request: UpdateUserRequest): Response<Unit>

    /**
     * 카카오 로그아웃을 수행합니다.
     */
    suspend fun logout()
}
