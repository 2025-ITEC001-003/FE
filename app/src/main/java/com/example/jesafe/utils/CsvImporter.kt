package com.example.jesafe.utils

import android.content.Context
import com.example.jesafe.data.local.AccidentEntity
import com.example.jesafe.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvImporter {

    suspend fun importFromCsv(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        // 기존 데이터 삭제 (개발 중 데이터 갱신용)
        db.accidentDao().deleteAll()

        val inputStream = context.assets.open("jeju_accidents.csv")
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        val accidentList = mutableListOf<AccidentEntity>()

        reader.readLines().drop(1).forEach { line ->
            val cols = line.split(',')

            if (cols.size >= 16) {
                try {
                    val entity = AccidentEntity(
                        reportNumber = cols.getOrNull(0)?.ifBlank { null },      // DCLR_NO
                        reportDate = cols.getOrNull(6)?.ifBlank { null },          // DCLR_YMD
                        season = cols.getOrNull(7)?.ifBlank { null },              // SEASN_NM
                        reportWeekday = cols.getOrNull(8)?.ifBlank { null },       // DCLR_DOW
                        longitude = cols.getOrNull(10)?.toDoubleOrNull(),          // ACDNT_OCRN_LOT
                        latitude = cols.getOrNull(11)?.toDoubleOrNull(),           // ACDNT_OCRN_LAT
                        accidentType = cols.getOrNull(5)?.ifBlank { null },        // ACDNT_INJR_NM
                        temperature = cols.getOrNull(12)?.toDoubleOrNull(),        // HR_UNIT_ARTMP
                        windSpeed = cols.getOrNull(14)?.toDoubleOrNull()           // HR_UNIT_WSPD
                    )
                    accidentList.add(entity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        db.accidentDao().insertAll(accidentList)
    }
}