package com.example.jesafe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accident")
data class AccidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val reportNumber: String?,
    val reportDate: String?,
    val season: String?,
    val reportWeekday: String?,
    val latitude: Double?,
    val longitude: Double?,
    val accidentType: String?,
    val temperature: Double?,
    val windSpeed: Double?
)