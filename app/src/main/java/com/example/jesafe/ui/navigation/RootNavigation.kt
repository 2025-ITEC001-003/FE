package com.example.jesafe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jesafe.ui.auth.AdditionalInfoScreen
import com.example.jesafe.ui.auth.LoginScreen
import com.example.jesafe.ui.auth.RegisterScreen
import com.example.jesafe.ui.auth.RegistrationCompleteScreen
import com.example.jesafe.ui.splash.SplashScreen

/**
 * 앱의 최상위 네비게이션 그래프를 정의합니다.
 * 스플래시 화면, 인증 관련 화면, 메인 화면 간의 흐름을 관리합니다.
 */
@Composable
fun RootNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("main") {
            AppNavigation()
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable(
            "additional_info/{nickname}",
            arguments = listOf(navArgument("nickname") { type = NavType.StringType })
        ) {
            val nickname = it.arguments?.getString("nickname") ?: ""
            AdditionalInfoScreen(navController, nickname)
        }
        composable(
            "registration_complete/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) {
            val name = it.arguments?.getString("name") ?: ""
            RegistrationCompleteScreen(navController, name)
        }
    }
}
