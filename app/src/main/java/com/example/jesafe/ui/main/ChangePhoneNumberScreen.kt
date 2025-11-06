package com.example.jesafe.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.ui.theme.JeSafeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePhoneNumberScreen(navController: NavController) {
    var phoneNumber by remember { mutableStateOf("") }
    val isPhoneNumberValid = phoneNumber.length > 10

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("전화번호 변경") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White, titleContentColor = Color.Black)
            )
        },
        bottomBar = {
            Button(
                onClick = { /* TODO: Handle phone number verification */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isPhoneNumberValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A2A61), // Dark Blue for enabled
                    disabledContainerColor = Color(0xFFB0C4DE), // Light blue-gray for disabled
                    contentColor = Color.White,
                    disabledContentColor = Color.White
                ),
                shape = RectangleShape
            ) {
                Text("인증번호 전송", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "해당 번호로 인증번호가 전송됩니다.",
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(40.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("+82") // Country code, could be a dropdown later
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("010-") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        if (phoneNumber.isNotEmpty()) {
                            IconButton(onClick = { phoneNumber = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Black,
                        unfocusedIndicatorColor = Color.LightGray,
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePhoneNumberScreenPreview() {
    JeSafeTheme {
        ChangePhoneNumberScreen(rememberNavController())
    }
}
