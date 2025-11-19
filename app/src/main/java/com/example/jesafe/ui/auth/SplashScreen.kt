package com.example.jesafe.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 앱 시작 시 로그인 상태를 확인하고 적절한 화면으로 리디렉션하는 동안 표시되는
 * 임시 스플래시 화면입니다. 이 화면 자체는 사용자에게 거의 보이지 않습니다.
 */
@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize()) // Just an empty box
}
