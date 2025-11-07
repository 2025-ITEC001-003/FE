package com.example.jesafe.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var name by remember { mutableStateOf("WWIT") }
    var phoneNumber by remember { mutableStateOf("010-xxxx-0864") }
    var email by remember { mutableStateOf("@gmail.com") }
    var diseaseInfo by remember { mutableStateOf("고혈압, 당뇨 등") }

    val navBackStackEntry = rootNavController.currentBackStackEntry

    LaunchedEffect(navBackStackEntry) {
        val newName = navBackStackEntry?.savedStateHandle?.get<String>("newName")
        if (newName != null) {
            name = newName
            navBackStackEntry.savedStateHandle.remove<String>("newName")
        }

        val newEmail = navBackStackEntry?.savedStateHandle?.get<String>("newEmail")
        if (newEmail != null) {
            email = newEmail
            navBackStackEntry.savedStateHandle.remove<String>("newEmail")
        }

        val newDiseaseInfo = navBackStackEntry?.savedStateHandle?.get<String>("newDiseaseInfo")
        if (newDiseaseInfo != null) {
            diseaseInfo = newDiseaseInfo
            navBackStackEntry.savedStateHandle.remove<String>("newDiseaseInfo")
        }

        val newPhoneNumber = navBackStackEntry?.savedStateHandle?.get<String>("newPhoneNumber")
        if (newPhoneNumber != null) {
            phoneNumber = newPhoneNumber
            navBackStackEntry.savedStateHandle.remove<String>("newPhoneNumber")
        }
    }
    val maskedPhoneNumber = remember(phoneNumber) {
        val parts = phoneNumber.split("-")
        if (parts.size == 3 && parts[1].length == 4) {
            "${parts[0]}-****-${parts[2]}"
        } else {
            phoneNumber
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 정보", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF2F2F7) // A light gray that matches system UI
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Section: 내 정보 관리
            SectionHeader(text = "내 정보 관리")
            Column(Modifier.background(Color.White)) {
                MyInfoRow(
                    title = "이름",
                    subtitle = name,
                    onClick = { rootNavController.navigate("change_name") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "휴대폰 번호",
                    subtitle = maskedPhoneNumber,
                    onClick = { rootNavController.navigate("change_phone_number") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "이메일",
                    subtitle = email,
                    onClick = { rootNavController.navigate("change_email") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "기저질환 관리",
                    subtitle = diseaseInfo,
                    onClick = { rootNavController.navigate("underlying_disease") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "비밀번호 변경",
                    onClick = { rootNavController.navigate("change_password") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "알림 설정",
                    onClick = { rootNavController.navigate("notification_settings") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: 개인 설정 관리
            SectionHeader(text = "개인 설정 관리")
            Column(Modifier.background(Color.White)) {
                MyInfoRow(title = "즐겨찾는 장소 관리")
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(title = "로그아웃", onClick = {
                    rootNavController.navigate("login") {
                        popUpTo(rootNavController.graph.id) {
                            inclusive = true
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        fontSize = 13.sp,
        color = Color.Gray
    )
}

@Composable
private fun MyInfoRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    actionContent: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            if (subtitle != null) {
                // With subtitle
                Text(text = title, fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
                    if (actionContent != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        actionContent(this)
                    }
                }
            } else {
                // Title only
                Text(text = title, fontSize = 16.sp, color = Color.Black)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyInfoScreenPreview() {
    JeSafeTheme {
        MyInfoScreen(rememberNavController())
    }
}
