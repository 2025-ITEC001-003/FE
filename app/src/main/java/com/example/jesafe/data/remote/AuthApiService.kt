package com.example.jesafe.data.remote

import com.example.jesafe.data.model.AuthRequest
import com.example.jesafe.data.model.AuthResponse
import com.example.jesafe.data.model.UpdateUserRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * 인증 및 사용자 정보 관련 API 엔드포인트를 정의하는 Retrofit 서비스 인터페이스입니다.
 */
interface AuthApiService {

    /**
     * 카카오 로그인을 통해 받은 인가 코드를 서버로 보내고, 자체 인증 토큰을 발급받습니다.
     */
    @POST("/api/kakao/login")
    suspend fun kakaoLogin(@Body request: AuthRequest): Response<AuthResponse>

    /**
     * 사용자의 추가 정보(출생년도, 성별, 기저질환)를 서버에 업데이트합니다.
     * 요청 시 반드시 헤더에 "Authorization: Bearer {TOKEN}" 형식으로 JWT를 포함해야 합니다.
     */
    @PUT("/api/user/info") // 백엔드에 이 엔드포인트 구현이 필요합니다.
    suspend fun updateUserInfo(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): Response<Unit> // 성공 시 별도 내용이 없으므로 Unit으로 받습니다.
}
