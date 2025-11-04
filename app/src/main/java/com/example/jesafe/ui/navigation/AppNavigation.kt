package com.example.jesafe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.ui.auth.AdditionalInfoScreen
import com.example.jesafe.ui.auth.LoginScreen
import com.example.jesafe.ui.auth.RegisterScreen
import com.example.jesafe.ui.auth.RegistrationCompleteScreen
import com.example.jesafe.ui.main.MainScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(rootNavController = navController)
        }
        // 모든 인증 관련 화면을 AppNavigation에 직접 등록합니다.
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("additional_info") {
            AdditionalInfoScreen(navController = navController)
        }
        composable("registration_complete") {
            RegistrationCompleteScreen(navController = navController)
        }
    }
}
