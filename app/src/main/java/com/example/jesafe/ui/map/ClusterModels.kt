package com.example.jesafe.ui.map

import com.example.jesafe.data.local.AccidentEntity
import com.google.android.gms.maps.model.LatLng

/**
 * 클러스터링된 개별 사고 데이터 포인트
 */
data class ClusteredPoint(
    val accident: AccidentEntity,
    val clusterId: Int,
    val isNoise: Boolean,
    val position: LatLng
)

/**
 * 클러스터 영역(원) 및 통계 정보
 */
data class ClusterArea(
    val clusterId: Int,
    val center: LatLng,
    val radius: Double,
    val maxDistance: Double,
    val count: Int,
    val stats: ClusterStats
)

/**
 * 클러스터 내부 통계 정보
 */
data class ClusterStats(
    val diseaseStats: Map<String, Float>,
    val severityStats: Map<String, Float>,
    val topMonths: List<Pair<String, Float>>,
    val topSeasons: List<Pair<String, Float>>
)
