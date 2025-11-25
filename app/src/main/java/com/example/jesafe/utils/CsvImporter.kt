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
        db.accidentDao().deleteAll()

        val inputStream = context.assets.open("jeju_accidents.csv")
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        val accidentList = mutableListOf<AccidentEntity>()

        reader.readLines().drop(1).forEach { line ->
            val cols = line.split(',')

            if (cols.size >= 16) {
                try {
                    val entity = AccidentEntity(
                        reportNumber = cols.getOrNull(0)?.ifBlank { null },
                        ptnOcrnTypeNm = cols.getOrNull(1)?.ifBlank { null },
                        ptnSymSeNm = cols.getOrNull(2)?.ifBlank { null },
                        srilOncrNm = cols.getOrNull(3)?.ifBlank { null },
                        trfcAcdntSeNm = cols.getOrNull(4)?.ifBlank { null },
                        accidentType = cols.getOrNull(5)?.ifBlank { null },
                        reportDate = cols.getOrNull(6)?.ifBlank { null },
                        season = cols.getOrNull(7)?.ifBlank { null },
                        reportWeekday = cols.getOrNull(8)?.ifBlank { null },
                        longitude = cols.getOrNull(10)?.toDoubleOrNull(),
                        latitude = cols.getOrNull(11)?.toDoubleOrNull(),
                        temperature = cols.getOrNull(12)?.toDoubleOrNull(),
                        windSpeed = cols.getOrNull(14)?.toDoubleOrNull()
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