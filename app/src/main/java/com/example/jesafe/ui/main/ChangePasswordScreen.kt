package com.example.jesafe.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.R
import com.example.jesafe.ui.theme.JeSafeTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.jesafe.ui.theme.*


private const val MIN_PASSWORD_LENGTH = 6
private const val MAX_PASSWORD_LENGTH = 20
private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$")
enum class PasswordScreenState {
    VERIFY_CURRENT,
    SET_NEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    val context = LocalContext.current
    var screenState by remember { mutableStateOf(PasswordScreenState.VERIFY_CURRENT) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }


    val currentPasswordError = if (currentPassword.isNotEmpty() && currentPassword.length < MIN_PASSWORD_LENGTH) {
        context.getString(R.string.error_password_too_short, MIN_PASSWORD_LENGTH)
    } else null
    val isCurrentPasswordValid = currentPassword.isNotEmpty() && currentPasswordError == null

    val newPasswordError = validateNewPassword(newPassword, context)
    val confirmPasswordError = validateConfirmPassword(newPassword, confirmPassword, context)
    val isNewPasswordValid = newPasswordError == null && confirmPasswordError == null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("비밀번호 변경") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            when (screenState) {
                PasswordScreenState.VERIFY_CURRENT -> {
                    Button(
                        onClick = { screenState = PasswordScreenState.SET_NEW },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isCurrentPasswordValid,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Navy,
                            disabledContainerColor = LightBlue,
                            contentColor = Color.White,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text("확인", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                PasswordScreenState.SET_NEW -> {
                    Button(
                        onClick = { navController.navigateUp() /* TODO: 서버/DB로 비밀번호 변경 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isNewPasswordValid,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Navy,
                            disabledContainerColor = LightBlue,
                            contentColor = Color.White,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text("비밀번호 변경", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            when (screenState) {
                PasswordScreenState.VERIFY_CURRENT -> {
                    Text(
                        text = "보안을 위해 현재 비밀번호를 입력해주세요.",
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    PasswordTextFieldWithValidation(
                        password = currentPassword,
                        onPasswordChange = { currentPassword = it },
                        passwordVisible = currentPasswordVisible,
                        onPasswordVisibilityChange = { currentPasswordVisible = it },
                        label = "현재 비밀번호",
                        errorMessage = currentPasswordError,
                        showCharCount = false
                    )
                }
                PasswordScreenState.SET_NEW -> {
                    Text(
                        text = "새로운 비밀번호를 입력해주세요.",
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    PasswordTextFieldWithValidation(
                        password = newPassword,
                        onPasswordChange = { newPassword = it },
                        passwordVisible = newPasswordVisible,
                        onPasswordVisibilityChange = { newPasswordVisible = it },
                        label = "새 비밀번호",
                        errorMessage = if (newPassword.isNotEmpty()) newPasswordError else null,
                        showCharCount = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PasswordTextFieldWithValidation(
                        password = confirmPassword,
                        onPasswordChange = { confirmPassword = it },
                        passwordVisible = confirmPasswordVisible,
                        onPasswordVisibilityChange = { confirmPasswordVisible = it },
                        label = "새 비밀번호 확인",
                        errorMessage = if (confirmPassword.isNotEmpty()) confirmPasswordError else null,
                        showCharCount = true
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordTextFieldWithValidation(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    label: String,
    errorMessage: String?,
    showCharCount: Boolean
) {
    val isError = errorMessage != null

    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                    Icon(painter = painterResource(id = image), contentDescription = "비밀번호 보기 토글")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = if (isError) Color.Red else Color.Black,
                unfocusedIndicatorColor = if (isError) Color.Red else Color.LightGray,
                cursorColor = if (isError) Color.Red else Color.Black
            ),
            isError = isError
        )

        Spacer(modifier = Modifier.height(8.dp)) // 텍스트필드와 글자 수/에러 메시지 간격

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 에러 메시지
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            // 글자 수 표시
            if (showCharCount) {
                Text(
                    text = "${password.length} / ${MAX_PASSWORD_LENGTH}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// 새 비밀번호 유효성 검사
private fun validateNewPassword(password: String, context: android.content.Context): String? {
    return when {
        password.length < MIN_PASSWORD_LENGTH -> context.getString(R.string.error_password_too_short, MIN_PASSWORD_LENGTH)
        password.length > MAX_PASSWORD_LENGTH -> context.getString(R.string.error_password_too_long, MAX_PASSWORD_LENGTH)
        !PASSWORD_REGEX.containsMatchIn(password) -> context.getString(R.string.error_password_invalid)
        else -> null
    }
}

// 비밀번호 확인 유효성
private fun validateConfirmPassword(newPassword: String, confirmPassword: String, context: android.content.Context): String? {
    if (newPassword != confirmPassword) return context.getString(R.string.error_password_not_match)
    return null
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    JeSafeTheme {
        ChangePasswordScreen(rememberNavController())
    }
}
