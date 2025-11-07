package com.example.jesafe.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.ui.theme.JeSafeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val initialPushState = savedStateHandle?.get<Boolean>("pushEnabled") ?: true
    val initialMarketingState = savedStateHandle?.get<Boolean>("marketingEnabled") ?: false

    var pushNotificationsEnabled by remember { mutableStateOf(initialPushState) }
    var marketingConsent by remember { mutableStateOf(initialMarketingState) }

    LaunchedEffect(Unit) {
        savedStateHandle?.remove<Boolean>("pushEnabled")
        savedStateHandle?.remove<Boolean>("marketingEnabled")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("알림 설정") },
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
                onClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("pushEnabled", pushNotificationsEnabled)
                    navController.previousBackStackEntry?.savedStateHandle?.set("marketingEnabled", marketingConsent)
                    navController.navigateUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A2A61)
                )
            ) {
                Text("저장", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            NotificationSwitch(
                title = "푸시 알림",
                subtitle = "사고 다발 지역 경보, 긴급 재난 정보 등 안전 관련 푸시 알림을 받습니다.",
                isChecked = pushNotificationsEnabled
            ) {
                pushNotificationsEnabled = it
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            NotificationSwitch(
                title = "마케팅 정보 수신 동의",
                subtitle = "이벤트 및 프로모션 등 다양한 혜택 정보를 받습니다.",
                isChecked = marketingConsent
            ) {
                marketingConsent = it
            }
        }
    }
}

@Composable
private fun NotificationSwitch(title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = Color.Gray)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationSettingsScreenPreview() {
    JeSafeTheme {
        NotificationSettingsScreen(rememberNavController())
    }
}
