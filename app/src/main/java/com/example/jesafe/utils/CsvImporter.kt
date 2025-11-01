package com.example.jesafe.utils

import android.content.Context
import com.example.jesafe.data.local.AccidentEntity
import com.example.jesafe.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvImporter {

    suspend fun importIfEmpty(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        if (db.accidentDao().getAll().isNotEmpty()) return@withContext

        val inputStream = context.assets.open("jeju_accidents.tsv")
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        val accidentList = mutableListOf<AccidentEntity>()

        reader.readLines().drop(1).forEach { line ->
            val cols = line.split('\t')

            if (cols.size >= 16) {
                try {
                    val entity = AccidentEntity(
                        symptomType = cols[0].ifBlank { null },              // PTN_SYM_SE_NM
                        severeInjury = cols[1].ifBlank { null },             // SRIL_ONCR_NM
                        trafficAccidentType = cols[2].ifBlank { null },      // TRFC_ACDNT_SE_NM
                        accidentType = cols[3].ifBlank { null },             // ACDNT_INJR_NM
                        reportDate = cols[4].ifBlank { null },               // DCLR_YMD
                        season = cols[5].ifBlank { null },                   // SEASN_NM
                        reportWeekday = cols[6].ifBlank { null },            // DCLR_DOW
                        gender = cols[7].ifBlank { null },                   // PTN_GNDR_NM
                        longitude = cols[8].toDoubleOrNull(),                // ACDNT_OCRN_LOT
                        latitude = cols[9].toDoubleOrNull(),                 // ACDNT_OCRN_LAT
                        temperature = cols[10].toDoubleOrNull(),             // HR_UNIT_ARTMP
                        rainfall = cols[11].toDoubleOrNull(),                // HR_UNIT_RN
                        windSpeed = cols[12].toDoubleOrNull(),               // HR_UNIT_WSPD
                        windDirection = cols[13].ifBlank { null },           // HR_UNIT_WNDRCT
                        humidity = cols[14].toDoubleOrNull(),                // HR_UNIT_HUM
                        snowfall = cols[15].toDoubleOrNull()                 // HR_UNIT_SNWFL
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