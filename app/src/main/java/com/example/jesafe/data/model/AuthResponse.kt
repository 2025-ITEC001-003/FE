package com.example.jesafe.data.model

import com.google.gson.annotations.SerializedName

/**
 * 서버로부터 받은 인증 토큰 및 사용자 상태 정보를 담는 응답 모델입니다.
 */
data class AuthResponse(
    @SerializedName("app_token")
    val accessToken: String,

    @SerializedName("is_new_user")
    val isNewUser: Boolean,

    @SerializedName("id")
    val id: Long,

    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("birth_year")
    val birthYear: Int?,

    @SerializedName("gender")
    val gender: String?,

    @SerializedName("disease_info")
    val diseaseInfo: String?,
    
    @SerializedName("phone")
    val phone: String?,

    @SerializedName("email")
    val email: String?
)
