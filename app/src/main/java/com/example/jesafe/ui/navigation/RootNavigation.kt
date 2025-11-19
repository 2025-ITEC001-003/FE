package com.example.jesafe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jesafe.ui.auth.AdditionalInfoContactScreen
import com.example.jesafe.ui.auth.AdditionalInfoDiseaseScreen
import com.example.jesafe.ui.auth.AdditionalInfoScreen
import com.example.jesafe.ui.auth.LoginScreen
import com.example.jesafe.ui.auth.LoginViewModel
import com.example.jesafe.ui.auth.RegisterScreen
import com.example.jesafe.ui.auth.RegistrationCompleteScreen
import com.example.jesafe.ui.splash.SplashScreen
import com.example.jesafe.ui.splash.SplashViewModel
import com.example.jesafe.viewmodel.AdditionalInfoViewModel

/**
 * 앱의 최상위 네비게이션 그래프를 정의합니다.
 * 스플래시 화면, 인증 관련 화면, 메인 화면 간의 흐름을 관리합니다.
 */
@Composable
fun RootNavigation() {
    val navController = rememberNavController()
    // 공통으로 사용할 ViewModel 인스턴스 생성
    val loginViewModel: LoginViewModel = hiltViewModel()
    val additionalInfoViewModel: AdditionalInfoViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController, viewModel = hiltViewModel<SplashViewModel>())
        }
        composable("login") {
            LoginScreen(navController, loginViewModel)
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
        ) { backStackEntry ->
            val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
            AdditionalInfoScreen(navController, nickname, additionalInfoViewModel)
        }
        composable(Routes.ADDITIONAL_INFO_CONTACT) {
            AdditionalInfoContactScreen(navController, additionalInfoViewModel)
        }
        composable(Routes.ADDITIONAL_INFO_DISEASE) {
            AdditionalInfoDiseaseScreen(navController, additionalInfoViewModel)
        }
        composable(
            "registration_complete/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            RegistrationCompleteScreen(navController, name)
        }
    }
}
