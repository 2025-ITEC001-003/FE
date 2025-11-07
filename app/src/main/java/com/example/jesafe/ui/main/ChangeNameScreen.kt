package com.example.jesafe.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.ui.theme.JeSafeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeNameScreen(navController: NavController) {
    var name by remember { mutableStateOf("김제주") }
    val maxNameLength = 10
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("이름 변경") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White, titleContentColor = Color.Black)
            )
        },
        bottomBar = {
            val isEnabled = name.isNotBlank()
            Button(
                onClick = {
                    if (isEnabled) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("newName", name)
                        navController.navigateUp()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A2A61), // Dark Blue for enabled
                    disabledContainerColor = Color(0xFFB0C4DE), // Light blue-gray for disabled
                    contentColor = Color.White,
                    disabledContentColor = Color.White
                ),
                shape = RectangleShape
            ) {
                Text("확인", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                text = "이름을 입력해 주세요.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            TextField(
                value = name,
                onValueChange = {
                    if (it.length <= maxNameLength) {
                        name = it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("이름 (예: 김제주)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                trailingIcon = {
                    if (name.isNotEmpty()) {
                        IconButton(onClick = { name = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.LightGray
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${name.length} / $maxNameLength",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

