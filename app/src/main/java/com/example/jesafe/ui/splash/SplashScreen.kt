package com.example.jesafe.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jesafe.R

/**
 * 스플래시 화면 Composable입니다.
 * 앱의 초기 인증 상태를 확인하고 적절한 화면으로 라우팅합니다.
 *
 * @param navController 메인 네비게이션 컨트롤러
 * @param viewModel 스플래시 화면의 로직을 관리하는 ViewModel
 */
@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // 인증된 상태 -> 메인 화면으로 이동
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true } // 스플래시 화면을 백스택에서 제거
                }
            }
            is AuthState.Unauthenticated -> {
                // 미인증 상태 -> 로그인 화면으로 이동
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true } // 스플래-시 화면을 백스택에서 제거
                }
            }
            else -> Unit // 로딩 중
        }
    }

    // 스플래시 화면 UI (예: 앱 로고)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // TODO: 추후 앱 로고 이미지 등으로 교체
        Text(text = stringResource(id = R.string.app_name))
    }
}
