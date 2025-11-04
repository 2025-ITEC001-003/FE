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

@Composable
fun RootNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable(
            "additional_info/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) {
            val name = it.arguments?.getString("name") ?: ""
            AdditionalInfoScreen(navController, name)
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
