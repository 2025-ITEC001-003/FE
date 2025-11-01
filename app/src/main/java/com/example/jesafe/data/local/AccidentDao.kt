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
            COUNT(*) AS count
        FROM accident
        WHERE latitude IS NOT NULL AND longitude IS NOT NULL
        GROUP BY latCenter, lonCenter
    """
    )
    fun getAccidentsGroupedByGrid(): List<GridCell>

    @Query("SELECT * FROM accident")
    fun getAll(): List<AccidentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accidents: List<AccidentEntity>)
}