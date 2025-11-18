package com.example.jesafe.data.model

import com.google.gson.annotations.SerializedName

/**
 * 카카오 인가 코드를 담아 서버에 보내는 요청 모델입니다.
 *
 * @property code 카카오 SDK로부터 받은 인가 코드
 */
data class AuthRequest(
    @SerializedName("code")
    val code: String
)
