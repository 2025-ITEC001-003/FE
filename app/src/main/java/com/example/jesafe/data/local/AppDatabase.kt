package com.example.jesafe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AccidentEntity::class, ChatEntity::class, ChatSessionEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accidentDao(): AccidentDao
    abstract fun chatDao(): ChatDao
}