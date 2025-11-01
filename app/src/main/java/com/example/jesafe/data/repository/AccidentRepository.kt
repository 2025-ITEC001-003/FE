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
    suspend fun insertAll(accidents: List<AccidentEntity>) = dao.insertAll(accidents)
}