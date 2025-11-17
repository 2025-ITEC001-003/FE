package com.example.jesafe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jesafe.ui.auth.AdditionalInfoScreen
import com.example.jesafe.ui.auth.LoginScreen
import com.example.jesafe.ui.auth.LoginViewModel
import com.example.jesafe.ui.auth.RegisterScreen
import com.example.jesafe.ui.auth.RegistrationCompleteScreen
import com.example.jesafe.ui.main.ChangeEmailScreen
import com.example.jesafe.ui.main.ChangeNameScreen
import com.example.jesafe.ui.main.ChangePasswordScreen
import com.example.jesafe.ui.main.ChangePhoneNumberScreen
import com.example.jesafe.ui.main.MainScreen
import com.example.jesafe.ui.main.NotificationSettingsScreen
import com.example.jesafe.ui.main.UnderlyingDiseaseScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // 여러 화면에서 공유할 LoginViewModel을 상위 레벨에서 생성
    val loginViewModel: LoginViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("main") {
            MainScreen(rootNavController = navController, loginViewModel = loginViewModel)
        }
        composable("login") {
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("change_name") {
            ChangeNameScreen(navController = navController)
        }
        composable("change_phone_number") {
            ChangePhoneNumberScreen(navController = navController)
        }
        composable("change_email") {
            ChangeEmailScreen(navController = navController)
        }
        composable("underlying_disease") {
            UnderlyingDiseaseScreen(navController = navController)
        }
        composable("change_password") {
            ChangePasswordScreen(navController = navController)
        }
        composable("notification_settings") {
            NotificationSettingsScreen(navController = navController)
        }
        // {name} 파라미터를 받을 수 있도록 경로 수정
        composable(
            route = "additional_info/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            AdditionalInfoScreen(navController = navController,  name = name)
        }
        // {name} 파라미터를 받을 수 있도록 경로 수정
        composable(
            route = "registration_complete/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            RegistrationCompleteScreen(navController = navController, name = name)
        }
    }
}
