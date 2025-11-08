package com.example.jesafe.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

enum class PasswordScreenState {
    VERIFY_CURRENT,
    SET_NEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    var screenState by remember { mutableStateOf(PasswordScreenState.VERIFY_CURRENT) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }


    val currentPasswordError = if (currentPassword.isNotEmpty() && currentPassword.length < 6) {
        "비밀번호는 6자 이상이어야 합니다"
    } else null
    val isCurrentPasswordValid = currentPassword.isNotEmpty() && currentPasswordError == null


    val newPasswordError = validateNewPassword(newPassword)
    val confirmPasswordError = validateConfirmPassword(newPassword, confirmPassword)
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
                            containerColor = Color(0xFF0A2A61),
                            disabledContainerColor = Color(0xFFB0C4DE),
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
                            containerColor = Color(0xFF0A2A61),
                            disabledContainerColor = Color(0xFFB0C4DE),
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
                        validator = { currentPasswordError },
                        showError = false,
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
                        validator = { newPasswordError },
                        showError = newPassword.isNotEmpty(),
                        showCharCount = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PasswordTextFieldWithValidation(
                        password = confirmPassword,
                        onPasswordChange = { confirmPassword = it },
                        passwordVisible = confirmPasswordVisible,
                        onPasswordVisibilityChange = { confirmPasswordVisible = it },
                        label = "새 비밀번호 확인",
                        validator = { confirmPasswordError },
                        showError = confirmPassword.isNotEmpty(),
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
                    text = "${password.length} / 20",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// 새 비밀번호 유효성 검사
private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*]).+$")
private fun validateNewPassword(password: String): String? {
    if (password.length < 6) return "비밀번호는 6자 이상이어야 합니다"
    if (password.length > 20) return "비밀번호는 최대 20자까지 가능합니다"
    if (!PASSWORD_REGEX.containsMatchIn(password)) return "문자/숫자/특수문자를 모두 포함해야 합니다"
    return null
}

// 비밀번호 확인 유효성
private fun validateConfirmPassword(newPassword: String, confirmPassword: String): String? {
    if (newPassword != confirmPassword) return "비밀번호가 일치하지 않습니다"
    return null
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    JeSafeTheme {
        ChangePasswordScreen(rememberNavController())
    }
}
