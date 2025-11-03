package com.example.jesafe.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.jesafe.ui.theme.JeSafeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            isLoading
        }

        Handler(Looper.getMainLooper()).postDelayed({
            isLoading = false
        }, 1000)

        setContent {
            JeSafeTheme {
                LoginScreen()
            }
        }
    }
}