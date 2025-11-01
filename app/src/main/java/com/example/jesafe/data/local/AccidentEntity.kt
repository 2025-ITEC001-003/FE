package com.example.jesafe.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accident")
data class AccidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 사고 및 환자 관련
    val symptomType: String?,        // PTN_SYM_SE_NM - 환자증상구분명
    val severeInjury: String?,       // SRIL_ONCR_NM - 중증외상명
    val trafficAccidentType: String?,// TRFC_ACDNT_SE_NM - 교통사고구분명
    val accidentType: String?,       // ACDNT_INJR_NM - 사고종별명

    // 시간 및 계절
    val reportDate: String?,         // DCLR_YMD - 신고일자
    val season: String?,             // SEASN_NM - 계절명
    val reportWeekday: String?,      // DCLR_DOW - 신고요일

    // 환자 정보
    val gender: String?,             // PTN_GNDR_NM - 환자성별명

    // 장소
    val longitude: Double?,          // ACDNT_OCRN_LOT - 사고발생경도
    val latitude: Double?,           // ACDNT_OCRN_LAT - 사고발생위도

    // 날씨 관련
    val temperature: Double?,        // HR_UNIT_ARTMP - 기온
    val rainfall: Double?,           // HR_UNIT_RN - 강수량
    val windSpeed: Double?,          // HR_UNIT_WSPD - 풍속
    val windDirection: String?,      // HR_UNIT_WNDRCT - 풍향
    val humidity: Double?,           // HR_UNIT_HUM - 습도
    val snowfall: Double?            // HR_UNIT_SNWFL - 적설량
)