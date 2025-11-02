package com.example.jesafe.ui.map

import com.example.jesafe.data.local.AccidentEntity
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * 클러스터링을 위한 AccidentEntity 래퍼 클래스
 */
data class AccidentClusterItem(
    private val accident: AccidentEntity
) : ClusterItem {

    override fun getPosition(): LatLng {
        return LatLng(accident.latitude ?: 0.0, accident.longitude ?: 0.0)
    }

    override fun getTitle(): String {
        return accident.accidentType ?: "사고"
    }

    override fun getSnippet(): String {
        return buildString {
            accident.reportDate?.let { append("날짜: $it\n") }
            accident.season?.let { append("계절: $it\n") }
            accident.temperature?.let { append("기온: ${it}°C") }
        }.trim()
    }

    override fun getZIndex(): Float {
        return 0f
    }

    // 원본 데이터 접근용
    fun getAccident(): AccidentEntity = accident
}

