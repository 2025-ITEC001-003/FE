package com.example.jesafe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.jesafe.ui.map.MapScreen
import com.example.jesafe.ui.map.MapViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MapViewModel by viewModels()

    // This flag will be used to keep the splash screen on.
    private var isLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Keep the splash screen visible for this Activity.
        splashScreen.setKeepOnScreenCondition {
            isLoading
        }

        // Use a Handler to delay the dismissal of the splash screen.
        Handler(Looper.getMainLooper()).postDelayed({
            isLoading = false
        }, 1000) // 1 second delay

        setContent {
            MapScreen(viewModel = viewModel)
        }
    }
}