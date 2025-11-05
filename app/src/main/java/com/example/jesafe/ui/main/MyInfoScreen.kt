package com.example.jesafe.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.ui.theme.JeSafeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInfoScreen(rootNavController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "로그인",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                modifier = Modifier.clickable { rootNavController.navigate("login") } // "auth" 대신 "login"으로 수정
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoButton(text = "채팅 문의", modifier = Modifier.weight(1f))
                InfoButton(text = "자주 묻는 질문", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 안전 정보 섹션
            SettingsMenuItem(text = "안전 정보") { /* TODO */ }
            SettingsMenuItem(text = "상담 내역") { /* TODO */ }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // 설정 섹션
            SettingsMenuItem(text = "알림 설정") { /* TODO */ }
            SettingsMenuItem(text = "공지사항") { /* TODO */ }
            SettingsMenuItem(text = "앱 정보") { /* TODO */ }
        }
    }
}

@Composable
private fun InfoButton(text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = { /* TODO */ },
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.LightGray.copy(alpha = 0.3f),
            contentColor = Color.Black
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsMenuItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 18.sp, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Navigate",
            modifier = Modifier.size(24.dp),
            tint = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyInfoScreenPreview() {
    JeSafeTheme {
        MyInfoScreen(rootNavController = rememberNavController())
    }
}
