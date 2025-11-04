package com.example.jesafe.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.R
import com.example.jesafe.ui.theme.JeSafeTheme

sealed class BottomNavItem(val route: String, val resourceId: Int, val title: String) {
    object Home : BottomNavItem("home", R.drawable.ic_home, "홈")
    object AiChat : BottomNavItem("ai_chat", R.drawable.ic_chat, "AI 채팅")
    object MyInfo : BottomNavItem("my_info", R.drawable.ic_my_info, "내 정보")
}

@Composable
fun MainScreen(rootNavController: NavController) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(BottomNavItem.Home, BottomNavItem.AiChat, BottomNavItem.MyInfo)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = screen.resourceId), contentDescription = screen.title, modifier = Modifier.size(24.dp)) },
                        label = { Text(screen.title, fontSize = 11.sp) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2CA0F3),
                            selectedTextColor = Color(0xFF2CA0F3),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen()
            }
            composable(BottomNavItem.AiChat.route) {
                AiChatScreen()
            }
            composable(BottomNavItem.MyInfo.route) {
                MyInfoScreen(rootNavController = rootNavController)
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // 지도 대신 임시 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A284D))
        )
        TopContent()
        BottomInfoSheet()
    }
}

@Composable
fun TopContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)), shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(id = R.drawable.ic_warning), contentDescription = "Warning", tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("현재 서귀포시 사고 다발 경보 발령", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(painterResource(id = R.drawable.ic_close), contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFF2CA0F3), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(painterResource(id = R.drawable.ic_robot), contentDescription = "Bot", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text("근처 사고 다발 지역 알려줘", color = Color.LightGray, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(painterResource(id = R.drawable.ic_mic), contentDescription = "Mic", tint = Color.Gray, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                    Spacer(Modifier.width(8.dp))
                    Text("안전", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
            }
            Spacer(Modifier.width(8.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = R.drawable.ic_people), contentDescription = "Users", tint = Color(0xFF2CA0F3), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("127명 접속중", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun BoxScope.BottomInfoSheet() {
    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
        Card(
            // 수정: 하단 여백을 줄여 탭 바 바로 위에 위치하도록 조정
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text("현재 위치 안전도", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))){
                        Text("안전", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("92", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2CA0F3))
                        Text("안전지수", fontSize = 12.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("3", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("근처 위험", fontSize = 12.sp, color = Color.Gray) 
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("27", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("온도", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = R.drawable.ic_clock), contentDescription = "Update", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("마지막 업데이트: 2분 전", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        Image(
            painter = painterResource(id = R.drawable.ic_call),
            contentDescription = "Call",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 32.dp, bottom = 32.dp) 
                .size(60.dp)
                .clickable { /* TODO: Handle call click */ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    JeSafeTheme {
        MainScreen(rememberNavController())
    }
}
