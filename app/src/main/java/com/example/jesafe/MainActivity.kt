package com.example.jesafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.jesafe.ui.map.MapScreen
import com.example.jesafe.ui.map.MapViewModel
import com.example.jesafe.ui.theme.JeSafeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JeSafeTheme {
                MapScreen(viewModel = viewModel)
            }
        }
    }
}