package com.example.jesafe.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val nickname: String,
    val birthYear: Int? = null,
    val gender: String? = null,
    val diseaseInfo: String? = null,
    val phone: String? = null,
    val email: String? = null
)
