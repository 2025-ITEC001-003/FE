package com.example.jesafe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AccidentDao {
    @Query(
        """
        SELECT 
            ROUND(latitude / 0.05, 0) * 0.05 AS latCenter,
            ROUND(longitude / 0.05, 0) * 0.05 AS lonCenter,
            COUNT(*) AS accidentCount
        FROM accident
        WHERE latitude IS NOT NULL AND longitude IS NOT NULL
        GROUP BY latCenter, lonCenter
    """
    )
    suspend fun getAccidentsGroupedByGrid(): List<GridCell>

    @Query("SELECT * FROM accident")
    suspend fun getAll(): List<AccidentEntity>

    @Query(
        """
        SELECT * FROM accident 
        WHERE latitude IS NOT NULL 
        AND longitude IS NOT NULL
        AND latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLon AND :maxLon
        ORDER BY RANDOM()
        LIMIT :limit
        """
    )
    suspend fun getAccidentsInBoundsSampled(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        limit: Int
    ): List<AccidentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accidents: List<AccidentEntity>)
}