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

    val isCurrentPasswordValid = currentPassword.length >= 6
    val isNewPasswordValid = newPassword.length >= 6 && newPassword == confirmPassword

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("비밀번호 변경") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White, titleContentColor = Color.Black)
            )
        },
        bottomBar = {
            when (screenState) {
                PasswordScreenState.VERIFY_CURRENT -> {
                    Button(
                        onClick = { screenState = PasswordScreenState.SET_NEW },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = isCurrentPasswordValid,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0A2A61), // Dark Blue for enabled
                            disabledContainerColor = Color(0xFFB0C4DE), // Light blue-gray for disabled
                            contentColor = Color.White,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text("확인", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                PasswordScreenState.SET_NEW -> {
                    Button(
                        onClick = { navController.navigateUp() /* TODO: Handle password change */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = isNewPasswordValid,
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0A2A61), // Dark Blue for enabled
                            disabledContainerColor = Color(0xFFB0C4DE), // Light blue-gray for disabled
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
                    PasswordTextField(
                        password = currentPassword,
                        onPasswordChange = { currentPassword = it },
                        passwordVisible = currentPasswordVisible,
                        onPasswordVisibilityChange = { currentPasswordVisible = it },
                        label = "비밀번호 (6자 이상)"
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
                    PasswordTextField(
                        password = newPassword,
                        onPasswordChange = { newPassword = it },
                        passwordVisible = newPasswordVisible,
                        onPasswordVisibilityChange = { newPasswordVisible = it },
                        label = "새 비밀번호 (6자 이상)"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PasswordTextField(
                        password = confirmPassword,
                        onPasswordChange = { confirmPassword = it },
                        passwordVisible = confirmPasswordVisible,
                        onPasswordVisibilityChange = { confirmPasswordVisible = it },
                        label = "새 비밀번호 확인"
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    label: String
) {
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
            focusedIndicatorColor = Color.Black,
            unfocusedIndicatorColor = Color.LightGray
        )
    )
}


@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    JeSafeTheme {
        ChangePasswordScreen(rememberNavController())
    }
}
