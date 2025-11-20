package com.example.jesafe.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jesafe.ui.components.LoadingIndicator
import com.example.jesafe.ui.main.MyInfoScreen
import com.example.jesafe.ui.navigation.Routes
import com.example.jesafe.ui.theme.JeSafeTheme
import com.example.jesafe.viewmodel.AdditionalInfoViewModel
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
 * 화면의 빈 공간을 터치하면 키보드가 내려가는 기능을 포함합니다.
 */
@Composable
fun AuthNavigation() {
    val navController: NavHostController = rememberNavController()
    // 공통 ViewModel 생성
    val loginViewModel: LoginViewModel = hiltViewModel()
    val additionalInfoViewModel: AdditionalInfoViewModel = hiltViewModel()
    val focusManager = LocalFocusManager.current

    val loginState by loginViewModel.loginState.collectAsState()

    // 앱 시작 시 로그인 상태를 확인하고 올바른 화면으로 리디렉션
    LaunchedEffect(loginState) {
        val route = when (loginState) {
            is LoginState.ExistingUser -> Routes.MAIN // 또는 "my_info"
            is LoginState.Initial -> Routes.LOGIN
            else -> null
        }
        route?.let {
            navController.navigate(it) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        NavHost(navController = navController, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) {
                SplashScreen()
            }
            composable(Routes.LOGIN) {
                LoginScreen(navController = navController, viewModel = loginViewModel)
            }
            composable(Routes.REGISTER) {
                RegisterScreen(navController = navController)
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
            composable(route = "my_info") { // TODO: Routes에 경로 추가
                MyInfoScreen(rootNavController = navController, loginViewModel = loginViewModel)
            }
        }
        LoadingIndicator()
    }
}
