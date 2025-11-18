package com.example.jesafe.ui.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.R
import com.example.jesafe.ui.theme.JeSafeTheme
import com.kakao.sdk.auth.AuthCodeClient
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
/**
 * 사용자 로그인 및 회원가입 화면을 표시합니다.
 *
 * 이메일/비밀번호 입력, 카카오 소셜 로그인, 회원가입 화면으로의 이동 기능을 제공합니다.
 * ViewModel의 [LoginState]를 관찰하여 로그인 결과에 따라 메인 화면 또는 추가 정보 입력 화면으로 자동 이동합니다.
 *
 * @param navController 앱의 화면 전환을 관리하는 NavController.
 * @param viewModel 로그인 로직 및 상태를 관리하는 [LoginViewModel].
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.ExistingUser -> {
                snackbarHostState.showSnackbar("로그인 성공!")
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            }

            is LoginState.NewUser -> {
                val nickname = state.user.nickname ?: "사용자"
                navController.navigate("additional_info/$nickname") {
                    popUpTo("login") { inclusive = true }
                }
            }

            is LoginState.Error -> {
                snackbarHostState.showSnackbar(state.message)
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
                .padding(horizontal = 24.dp),
        ) {

            Text("로그인", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(48.dp))

            Text("Email", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("example.mail.com", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(10.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("비밀번호", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("••••••••••••", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = painterResource(id = image), contentDescription = null)
                    }
                },
                shape = RoundedCornerShape(10.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2CA0F3))
            ) {
                Text("로그인", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 카카오 로그인 버튼 UI와 로직 결합
            Button(
                onClick = {
                    val authCodeCallback: (String?, Throwable?) -> Unit = { code, error ->
                        if (error != null) {
                            Log.e("KAKAO_LOGIN", "인가 코드 발급 실패", error)
                            scope.launch { snackbarHostState.showSnackbar("로그인에 실패했습니다: ${error.message}") }
                        } else if (code != null) {
                            Log.i("KAKAO_LOGIN", "인가 코드 발급 성공: $code")
                            viewModel.requestKakaoLoginWithAuthCode(code)
                        }
                    }

                    if (AuthCodeClient.instance.isKakaoTalkLoginAvailable(context)) {
                        AuthCodeClient.instance.authorizeWithKakaoTalk(context) { code, error ->
                            if (error != null) {
                                Log.e("KAKAO_LOGIN", "카카오톡으로 인가 코드 받기 실패", error)

                                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                                    return@authorizeWithKakaoTalk
                                }

                                AuthCodeClient.instance.authorizeWithKakaoAccount(
                                    context,
                                    callback = authCodeCallback
                                )
                            } else if (code != null) {
                                authCodeCallback(code, null)
                            }
                        }
                    } else {
                        AuthCodeClient.instance.authorizeWithKakaoAccount(context, callback = authCodeCallback)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),    // 내부 여백 제거
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Unspecified
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kakao_login_large_wide),
                    contentDescription = "카카오톡으로 간편 로그인",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("계정이 없다면?", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = { navController.navigate("register") }) {
                    Text("회원가입", color = Color(0xFF2CA0F3), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (loginState == LoginState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
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
