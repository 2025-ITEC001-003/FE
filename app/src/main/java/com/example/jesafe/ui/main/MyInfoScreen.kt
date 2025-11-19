package com.example.jesafe.ui.main

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jesafe.ui.auth.LoginViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInfoScreen(
    rootNavController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel(),
    myInfoViewModel: MyInfoViewModel = hiltViewModel()
) {
    val user by myInfoViewModel.user.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("정말 로그아웃 하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        coroutineScope.launch {
                            loginViewModel.logout()
                            rootNavController.navigate("login") {
                                popUpTo(rootNavController.graph.id) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 정보", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF2F2F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(text = "내 정보 관리")
            Column(Modifier.background(Color.White)) {
                MyInfoRow(
                    title = "이름(닉네임)",
                    subtitle = user?.nickname ?: "로그인 정보 없음",
                    onClick = { rootNavController.navigate("change_name") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "휴대폰 번호",
                    subtitle = user?.phone?.let { maskPhoneNumber(it) } ?: "정보 없음",
                    onClick = { rootNavController.navigate("change_phone_number") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "이메일",
                    subtitle = user?.email ?: "정보 없음",
                    onClick = { rootNavController.navigate("change_email") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "기저질환 관리",
                    subtitle = user?.diseaseInfo ?: "정보 없음",
                    onClick = { rootNavController.navigate("underlying_disease") }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "알림 설정",
                    onClick = { rootNavController.navigate("notification_settings") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(text = "개인 설정 관리")
            Column(Modifier.background(Color.White)) {
                MyInfoRow(title = "즐겨찾는 장소 관리")
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), modifier = Modifier.padding(start = 16.dp))
                MyInfoRow(
                    title = "로그아웃",
                    onClick = { showLogoutDialog = true }
                )
            }
        }
    }
}

private fun maskPhoneNumber(phoneNumber: String): String {
    val parts = phoneNumber.split("-")
    return if (parts.size == 3 && parts[1].length == 4) {
        "${parts[0]}-****-${parts[2]}"
    } else {
        phoneNumber
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
