package com.example.jesafe.data.model

import com.google.gson.annotations.SerializedName

/**
 * 사용자 추가 정보(출생년도, 성별, 기저질환)를 서버에 업데이트하기 위한 요청 모델
 */
data class UpdateUserRequest(
    @SerializedName("birth_year")
    val birthYear: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("disease_info")
    val diseaseInfo: String? // "고혈압, 당뇨" 또는 null
)
