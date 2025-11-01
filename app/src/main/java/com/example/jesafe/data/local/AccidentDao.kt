package com.example.jesafe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AccidentDao {
    @Query("SELECT * FROM accident")
    suspend fun getAll(): List<AccidentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accidents: List<AccidentEntity>)
}