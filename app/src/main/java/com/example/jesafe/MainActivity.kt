package com.example.jesafe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.jesafe.ui.navigation.AppNavigation
import com.example.jesafe.ui.theme.JeSafeTheme
import dagger.hilt.android.AndroidEntryPoint

private object MainActivityDefaults {
    /** 스플래시 화면이 표시되는 최소 시간 (ms) */
    const val SPLASH_DISPLAY_TIME = 1000L
}

/**
 * 앱의 메인 액티비티입니다.
 * 앱의 진입점 역할을 하며, 스플래시 화면을 표시하고 메인 UI를 설정합니다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isLoading = true

    /**
     * 액티비티가 생성될 때 호출됩니다.
     *
     * 1. Core Splashscreen API를 사용하여 스플래시 화면을 설치합니다.
     * 2. 초기 데이터 로딩이 완료될 때까지 스플래시 화면이 계속 표시되도록 설정합니다.
     * 3. Jetpack Compose를 사용하여 메인 UI(`AppNavigation`)를 설정합니다.
     *
     * @param savedInstanceState 액티비티가 이전에 종료된 경우, 마지막 상태를 포함하는 Bundle 객체입니다.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 초기 데이터 로딩이 끝날 때까지 스플래시 화면을 유지합니다.
        splashScreen.setKeepOnScreenCondition {
            isLoading
        }

        // 최소 표시 시간 후, 로딩 상태를 false로 변경하여 스플래시 화면을 종료합니다.
        Handler(Looper.getMainLooper()).postDelayed({
            isLoading = false
        }, MainActivityDefaults.SPLASH_DISPLAY_TIME)

        setContent {
            JeSafeTheme {
                AppNavigation()
            }
        }
    }
}
