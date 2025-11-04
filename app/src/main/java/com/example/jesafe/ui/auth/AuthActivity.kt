package com.example.jesafe.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jesafe.ui.main.MyInfoScreen
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
                AuthNavigation()
            }
        }
    }
}

@Composable
fun AuthNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "my_info") {
        composable(route = "login") {
            LoginScreen(navController = navController)
        }

        composable(route = "register") {
            RegisterScreen(navController = navController)
        }

        composable(
            route = "additional_info/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) {
            val name = it.arguments?.getString("name") ?: ""
            AdditionalInfoScreen(navController = navController, name = name)
        }

        composable(
            route = "registration_complete/{name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType })
        ) {
            val name = it.arguments?.getString("name") ?: ""
            RegistrationCompleteScreen(navController = navController, name = name)
        }

        composable(route = "my_info") {
            MyInfoScreen(rootNavController = navController)
        }
    }
}
