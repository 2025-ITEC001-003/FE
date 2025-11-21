package com.example.jesafe.data.model

import com.google.gson.annotations.SerializedName

/**
 * 사용자 추가 정보를 서버에 업데이트하기 위한 요청 모델
 */
data class UpdateUserRequest(
    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("birth_year")
    val birthYear: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("disease_info")
    val diseaseInfo: String?
)
