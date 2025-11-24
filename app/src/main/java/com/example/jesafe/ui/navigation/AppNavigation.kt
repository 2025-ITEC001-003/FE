package com.example.jesafe.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.jesafe.ui.auth.LoginState
import com.example.jesafe.ui.auth.LoginViewModel
import com.example.jesafe.ui.auth.RegisterScreen
import com.example.jesafe.ui.auth.RegistrationCompleteScreen
import com.example.jesafe.ui.auth.SplashScreen
import com.example.jesafe.ui.chatbot.ChatDetailScreen
import com.example.jesafe.ui.components.LoadingIndicator
import com.example.jesafe.ui.main.ChangeEmailScreen
import com.example.jesafe.ui.main.ChangeNameScreen
import com.example.jesafe.ui.main.ChangePasswordScreen
import com.example.jesafe.ui.main.ChangePhoneNumberScreen
import com.example.jesafe.ui.main.MainScreen
import com.example.jesafe.ui.main.NotificationSettingsScreen
import com.example.jesafe.ui.main.UnderlyingDiseaseScreen
import com.example.jesafe.viewmodel.AdditionalInfoViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // 공통 ViewModel 생성
    val loginViewModel: LoginViewModel = hiltViewModel()
    val additionalInfoViewModel: AdditionalInfoViewModel = hiltViewModel()

    val loginState by loginViewModel.loginState.collectAsState()

    // 앱 시작 시 로그인 상태를 확인하고 올바른 화면으로 리디렉션
    LaunchedEffect(loginState) {
        val route = when (loginState) {
            is LoginState.ExistingUser -> Routes.MAIN
            is LoginState.Initial -> Routes.LOGIN
            else -> null // Loading, NewUser, Error 등의 상태에서는 아무것도 하지 않음
        }
        route?.let {
            navController.navigate(it) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) {
                SplashScreen()
            }
            composable(Routes.MAIN) {
                MainScreen(rootNavController = navController, loginViewModel = loginViewModel)
            }
            composable(Routes.LOGIN) {
                LoginScreen(navController = navController, viewModel = loginViewModel)
            }
            composable(Routes.REGISTER) {
                RegisterScreen(navController = navController)
            }
            composable(
                route = "${Routes.CHANGE_NAME}/{currentName}",
                arguments = listOf(navArgument("currentName") { type = NavType.StringType })
            ) { backStackEntry ->
                val currentName = backStackEntry.arguments?.getString("currentName") ?: ""
                ChangeNameScreen(navController = navController, initialName = currentName)
            }
            composable(Routes.CHANGE_PHONE_NUMBER) {
                ChangePhoneNumberScreen(navController = navController)
            }
            composable(Routes.CHANGE_EMAIL) {
                ChangeEmailScreen(navController = navController)
            }
            composable(Routes.UNDERLYING_DISEASE) {
                UnderlyingDiseaseScreen(navController = navController)
            }
            composable(Routes.CHANGE_PASSWORD) {
                ChangePasswordScreen(navController = navController)
            }
            composable(Routes.NOTIFICATION_SETTINGS) {
                NotificationSettingsScreen(navController = navController)
            }
            composable(
                route = Routes.ADDITIONAL_INFO,
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name") ?: ""
                AdditionalInfoScreen(navController = navController, name = name, viewModel = additionalInfoViewModel)
            }
            composable(Routes.ADDITIONAL_INFO_CONTACT) {
                AdditionalInfoContactScreen(navController = navController, viewModel = additionalInfoViewModel)
            }
            composable(Routes.ADDITIONAL_INFO_DISEASE) {
                AdditionalInfoDiseaseScreen(navController = navController, viewModel = additionalInfoViewModel)
            }
            composable(
                route = Routes.REGISTRATION_COMPLETE,
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val name = backStackEntry.arguments?.getString("name") ?: ""
                RegistrationCompleteScreen(navController = navController, name = name)
            }
        }
        LoadingIndicator()
    }
}
