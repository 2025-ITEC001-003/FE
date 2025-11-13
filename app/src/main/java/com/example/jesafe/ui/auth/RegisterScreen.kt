package com.example.jesafe.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.R
import com.example.jesafe.ui.theme.JeSafeTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import com.example.jesafe.ui.theme.*

private const val MAX_NAME_LENGTH = 10
private const val MIN_PASSWORD_LENGTH = 6
private const val MAX_PASSWORD_LENGTH = 20
private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val nameError = if (name.isNotEmpty() && name.length > MAX_NAME_LENGTH) stringResource(R.string.error_name_max_length, MAX_NAME_LENGTH) else null
    val emailError = if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) stringResource(R.string.error_email_invalid) else null
    val passwordError = if (password.isNotEmpty()) validatePassword(password) else null

    val isButtonEnabled =
        name.isNotBlank() && email.isNotBlank() && password.isNotBlank() &&
                nameError == null && emailError == null && passwordError == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color.DarkGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                "정보를 입력해주세요!",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text("이름", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("USER", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                isError = nameError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = LightSkyBlue,
                    focusedBorderColor = SoftBlue,
                    errorBorderColor = Color.Red
                ),
                singleLine = true
            )
            if (nameError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(nameError, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Email", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("example@mail.com", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(10.dp),
                isError = emailError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = LightSkyBlue,
                    focusedBorderColor = SoftBlue,
                    errorBorderColor = Color.Red
                ),
                singleLine = true
            )
            if (emailError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(emailError, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("비밀번호", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("••••••••", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(painter = painterResource(id = image), contentDescription = null)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    isError = passwordError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = LightSkyBlue,
                        focusedBorderColor = SoftBlue,
                        errorBorderColor = Color.Red
                    ),
                    singleLine = true
                )
                if (passwordError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(passwordError, color = Color.Red, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { navController.navigate("additional_info/$name") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftBlue),
                    enabled = isButtonEnabled
                ) {
                    Text("다음으로", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("계정이 있다면?", color = Color.Gray, fontSize = 14.sp)
                    TextButton(onClick = { navController.navigate("login") }) {
                        Text("로그인", color = SoftBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
@Composable
private fun validatePassword(password: String): String? {
    return when {
        password.length < MIN_PASSWORD_LENGTH -> stringResource(R.string.error_password_too_short, MIN_PASSWORD_LENGTH)
        password.length > MAX_PASSWORD_LENGTH -> stringResource(R.string.error_password_too_long, MAX_PASSWORD_LENGTH)
        !PASSWORD_REGEX.containsMatchIn(password) -> stringResource(R.string.error_password_invalid)
        else -> null
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    JeSafeTheme {
        RegisterScreen(rememberNavController())
    }
}