package com.example.jesafe

import android.app.Application
import android.util.Log
import com.example.jesafe.data.local.AppDatabase
import com.example.jesafe.utils.CsvImporter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class JeSafeApp : Application() {
    companion object {
        private const val TAG = "JeSafeApp"
    }

    @Inject
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            CsvImporter.importIfEmpty(this@JeSafeApp, database)
            Log.d(TAG, "CSV import completed")
        }
    }
}