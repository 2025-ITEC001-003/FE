package com.example.jesafe

import android.app.Application
import android.util.Log
import com.example.jesafe.data.local.AppDatabase
import com.example.jesafe.utils.CsvImporter
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class JeSafeApp : Application() {
    companion object {
        private const val TAG = "JeSafeApp"
    }

    @Inject
    lateinit var database: AppDatabase

    // 애플리케이션 생명주기와 연결된 코루틴 스코프
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        KakaoSdk.init(this, "bb30f164f208df568bc0c2a1d39d20b0")

        applicationScope.launch {
            CsvImporter.importIfEmpty(this@JeSafeApp, database)
            Log.d(TAG, "CSV import completed")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // 애플리케이션 종료 시 코루틴 스코프 정리
        applicationScope.cancel()
        Log.d(TAG, "Application scope cancelled")
    }
}