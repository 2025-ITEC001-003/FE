package com.example.jesafe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accident")
data class AccidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val reportNumber: String? = null,
    val ptnOcrnTypeNm: String? = null,
    val ptnSymSeNm: String? = null,
    val srilOncrNm: String? = null,
    val trfcAcdntSeNm: String? = null,
    val reportDate: String? = null,
    val season: String? = null,
    val reportWeekday: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accidentType: String? = null,
    val temperature: Double? = null,
    val windSpeed: Double? = null
)
