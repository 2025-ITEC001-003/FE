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

/**
 * 인증 관련 화면(로그인, 회원가입 등)을 호스팅하는 메인 Activity입니다.
 * 앱 실행 시 스플래시 스크린을 표시하고, [AuthNavigation]을 설정합니다.
 */
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

/**
 * 인증 모듈의 네비게이션 그래프를 정의합니다.
 * [NavHost]를 사용하여 로그인, 회원가입, 추가 정보 입력 등의 화면 간 이동을 관리합니다.
 */
@Composable
fun AuthNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
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
