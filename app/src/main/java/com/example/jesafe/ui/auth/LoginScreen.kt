package com.example.jesafe.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.R
import com.example.jesafe.ui.navigation.Routes
import com.example.jesafe.ui.theme.Dimens
import com.example.jesafe.ui.theme.JeSafeTheme

@OptIn(ExperimentalMaterial3Api::class)
/**
 * 사용자 로그인 화면을 표시합니다.
 *
 * 카카오 소셜 로그인 기능을 제공하며, UI 이벤트를 ViewModel에 위임합니다.
 * ViewModel의 [LoginState]를 관찰하여 로그인 결과에 따라 화면을 전환하거나 스낵바를 표시합니다.
 *
 * @param navController 앱의 화면 전환을 관리하는 NavController.
 * @param viewModel 로그인 로직 및 상태를 관리하는 [LoginViewModel].
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()

    // 로그인 상태 변경에 따른 UI 처리
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.ExistingUser -> {
                snackbarHostState.showSnackbar(context.getString(R.string.login_success))
                navController.navigate(Routes.MAIN) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }

            is LoginState.NewUser -> {
                val nickname = state.user.nickname ?: "사용자"
                navController.navigate(Routes.additionalInfo(nickname)) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }

            is LoginState.Error -> {
                val message = state.message ?: context.getString(R.string.unknown_error)
                snackbarHostState.showSnackbar(message)
            }

            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.logo_image),
                contentDescription = stringResource(id = R.string.jesafe_app_icon),
                modifier = Modifier.size(Dimens.LoginIconSize),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(Dimens.LoginIconBottomMargin))
            Text(
                text = stringResource(id = R.string.login_welcome),
                fontWeight = FontWeight.Bold,
                fontSize = Dimens.LoginTitleFontSize,
                textAlign = TextAlign.Center,
                lineHeight = Dimens.LoginTitleLineHeight
            )
            Spacer(modifier = Modifier.weight(1f))

            // 카카오 로그인 버튼: 클릭 시 ViewModel에 로그인 요청 위임
            Button(
                onClick = { viewModel.startKakaoLogin(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.KakaoButtonHeight),
                shape = RoundedCornerShape(Dimens.KakaoButtonCornerRadius),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Unspecified
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kakao_login_large_wide),
                    contentDescription = stringResource(id = R.string.login_kakao),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(Dimens.KakaoButtonBottomMargin))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    JeSafeTheme {
        LoginScreen(rememberNavController())
    }
}
