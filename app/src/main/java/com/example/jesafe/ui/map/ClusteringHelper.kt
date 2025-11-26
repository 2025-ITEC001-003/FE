package com.example.jesafe.ui.map

import android.location.Location
import com.example.jesafe.data.local.AccidentEntity
import com.google.android.gms.maps.model.LatLng

object ClusteringHelper {

    private const val EPSILON_METERS = 500.0
    private const val MIN_POINTS = 3

    fun performClustering(accidents: List<AccidentEntity>): Pair<List<ClusterArea>, List<ClusteredPoint>> {
        val visited = BooleanArray(accidents.size)
        val clusterIds = IntArray(accidents.size) { -1 }
        var clusterIdCounter = 0

        for (i in accidents.indices) {
            if (visited[i]) continue
            visited[i] = true

            val neighbors = getNeighbors(accidents, i, EPSILON_METERS)
            
            if (neighbors.size < MIN_POINTS) {
                clusterIds[i] = -1 // Noise
            } else {
                clusterIdCounter++
                val currentClusterId = clusterIdCounter
                expandCluster(accidents, visited, clusterIds, i, neighbors, currentClusterId, EPSILON_METERS)
            }
        }

        val clusteredPoints = accidents.mapIndexed { index, accident ->
            val cId = clusterIds[index]
            val isNoise = cId == -1
            ClusteredPoint(
                accident = accident,
                clusterId = cId,
                isNoise = isNoise,
                position = LatLng(accident.latitude ?: 0.0, accident.longitude ?: 0.0)
            )
        }

        val clusterAreas = clusteredPoints
            .filter { !it.isNoise }
            .groupBy { it.clusterId }
            .map { (cId, points) ->
                calculateClusterArea(cId, points)
            }

        return Pair(clusterAreas, clusteredPoints)
    }

    private fun expandCluster(
        accidents: List<AccidentEntity>,
        visited: BooleanArray,
        clusterIds: IntArray,
        index: Int,
        neighbors: MutableList<Int>,
        clusterId: Int,
        epsilon: Double
    ) {
        clusterIds[index] = clusterId
        
        var i = 0
        while (i < neighbors.size) {
            val neighborIndex = neighbors[i]
            
            if (!visited[neighborIndex]) {
                visited[neighborIndex] = true
                val neighborNeighbors = getNeighbors(accidents, neighborIndex, epsilon)
                if (neighborNeighbors.size >= MIN_POINTS) {
                    neighbors.addAll(neighborNeighbors)
                }
            }
            
            if (clusterIds[neighborIndex] == -1) {
                clusterIds[neighborIndex] = clusterId
            }
            i++
        }
    }

    private fun getNeighbors(accidents: List<AccidentEntity>, index: Int, epsilon: Double): MutableList<Int> {
        val neighbors = mutableListOf<Int>()
        val center = accidents[index]
        
        for (i in accidents.indices) {
            if (i == index) continue
            if (calculateDistance(center, accidents[i]) <= epsilon) {
                neighbors.add(i)
            }
        }
        return neighbors
    }

    private fun calculateDistance(a: AccidentEntity, b: AccidentEntity): Float {
        val lat1 = a.latitude ?: return Float.MAX_VALUE
        val lon1 = a.longitude ?: return Float.MAX_VALUE
        val lat2 = b.latitude ?: return Float.MAX_VALUE
        val lon2 = b.longitude ?: return Float.MAX_VALUE

        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    private fun calculateClusterArea(clusterId: Int, points: List<ClusteredPoint>): ClusterArea {
        val total = points.size.toFloat()
        if (total == 0f) {
             return ClusterArea(clusterId, LatLng(0.0,0.0), 0.0, 0.0, 0, ClusterStats(emptyMap(), emptyMap(), emptyList(), emptyList()))
        }

        val avgLat = points.map { it.position.latitude }.average()
        val avgLon = points.map { it.position.longitude }.average()
        val center = LatLng(avgLat, avgLon)

        var maxDist = 0.0
        points.forEach { point ->
            val results = FloatArray(1)
            Location.distanceBetween(center.latitude, center.longitude, point.position.latitude, point.position.longitude, results)
            if (results[0] > maxDist) {
                maxDist = results[0].toDouble()
            }
        }
        val radius = if (maxDist == 0.0) 50.0 else maxDist + 20.0

        val diseaseCount = points.count { 
            it.accident.ptnOcrnTypeNm == "질병"
        }
        val diseaseStats = mapOf(
            "질병" to (diseaseCount / total * 100),
            "질병외" to ((points.size - diseaseCount) / total * 100)
        )

        val trafficCount = points.count { 
            !it.accident.trfcAcdntSeNm.isNullOrEmpty()
        }

        val severeCount = points.count { 
             val v = it.accident.srilOncrNm
             !v.isNullOrEmpty() && v.contains("중증")
        }
        
        val severityStats = mapOf(
            "교통사고" to (trafficCount / total * 100),
            "중증외상" to (severeCount / total * 100)
        )

        val topMonths = points.mapNotNull { 
            it.accident.reportDate?.takeIf { d -> d.length >= 6 }?.substring(4, 6)?.toIntOrNull()?.toString()?.plus("월") 
        }.groupingBy { it }.eachCount().entries
            .sortedByDescending { it.value }
            .map { it.key to (it.value / total * 100) }

        val topSeasons = points.mapNotNull { it.accident.season }
            .groupingBy { it }.eachCount().entries
            .sortedByDescending { it.value }
            .map { it.key to (it.value / total * 100) }

        return ClusterArea(
            clusterId = clusterId,
            center = center,
            radius = radius,
            maxDistance = maxDist,
            count = points.size,
            stats = ClusterStats(
                diseaseStats = diseaseStats,
                severityStats = severityStats,
                topMonths = topMonths,
                topSeasons = topSeasons
            )
        )
    }
}
