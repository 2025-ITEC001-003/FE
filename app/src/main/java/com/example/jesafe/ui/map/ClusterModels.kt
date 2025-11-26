package com.example.jesafe.ui.map

import com.example.jesafe.data.local.AccidentEntity
import com.google.android.gms.maps.model.LatLng

/**
 * 클러스터링된 개별 사고 데이터 포인트
 */
data class ClusteredPoint(
    val accident: AccidentEntity,
    val clusterId: Int, // 클러스터 ID (-1이면 노이즈로 취급 가능하지만, isNoise 필드 별도 존재)
    val isNoise: Boolean,
    val position: LatLng
)

/**
 * 클러스터 영역(원) 및 통계 정보
 */
data class ClusterArea(
    val clusterId: Int,
    val center: LatLng,
    val radius: Double, // 미터 단위 (반경)
    val maxDistance: Double, // 중심에서 가장 먼 점까지의 거리 (최대 거리)
    val count: Int,
    val stats: ClusterStats
)

/**
 * 클러스터 내부 통계 정보
 */
data class ClusterStats(
    val diseaseStats: Map<String, Float>, // 예: "질병" -> 31.4, "질병외" -> 68.6
    val severityStats: Map<String, Float>, // 예: "교통사고" -> 0.0, "중증외상" -> 5.7
    val topMonths: List<Pair<String, Float>>, // 상위 월 분포
    val topSeasons: List<Pair<String, Float>> // 상위 계절 분포
)
