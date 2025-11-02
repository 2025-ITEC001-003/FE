package com.example.jesafe.data.repository

import com.example.jesafe.data.local.AccidentDao
import com.example.jesafe.data.local.AccidentEntity
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AccidentRepository @Inject constructor(
    private val dao: AccidentDao
) {
    suspend fun getAllAccidents() = dao.getAll()

    suspend fun getAccidentsGroupedByGrid() = dao.getAccidentsGroupedByGrid()

    suspend fun getAccidentsInBoundsSampled(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        limit: Int
    ) = dao.getAccidentsInBoundsSampled(minLat, maxLat, minLon, maxLon, limit)

    suspend fun insertAll(accidents: List<AccidentEntity>) = dao.insertAll(accidents)
}