package com.example.jesafe.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    object ChatHistory : BottomNavItem("chat_history", R.drawable.ic_chat, "채팅 내역")
    object MyInfo : BottomNavItem("my_info", R.drawable.ic_my_info, "내 정보")
}

@Composable
fun MainScreen(rootNavController: NavController) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(BottomNavItem.Home, BottomNavItem.ChatHistory, BottomNavItem.MyInfo)

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
                HomeScreen(navController = navController)
            }
            composable(BottomNavItem.ChatHistory.route) {
                ChatHistoryScreen(navController = navController)
            }
            composable(BottomNavItem.MyInfo.route) {
                MyInfoScreen(rootNavController = rootNavController)
            }
            composable("new_chat") {
                AiChatScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 지도 대신 임시 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A284D))
        )
        TopContent(navController = navController)
        BottomInfoSheet()
    }
}

@Composable
fun TopContent(navController: NavController) {
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
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.clickable { navController.navigate("new_chat") }
        ) {
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

data class ChatRoom(val id: Int, val title: String, val lastMessage: String, val timestamp: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryScreen(navController: NavController) {
    // 샘플 데이터, "무한 스크롤"을 위해 아이템 개수 증가
    val chatRooms = List(100) { i ->
        ChatRoom(i, "제주도 동쪽 여행 계획 ${i + 1}", "네, 좋습니다. 추가로...", "오후 3:4${i % 10}")
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("new_chat") },
                containerColor = Color(0xFF2CA0F3),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            // Scaffold로부터 받은 패딩을 먼저 적용하여 UI 요소(FAB)와 겹치지 않도록 합니다.
            // 그 다음 fillMaxSize()를 호출하여 남은 공간을 모두 채웁니다.
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            // 리스트 내부의 콘텐츠에 대한 패딩을 설정합니다.
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatRooms) { room ->
                ChatHistoryItem(room)
            }
        }
    }
}

@Composable
fun ChatHistoryItem(room: ChatRoom) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = R.drawable.ic_chat), contentDescription = "Chat Icon", tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(room.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(room.lastMessage, fontSize = 14.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(16.dp))
            Text(room.timestamp, fontSize = 12.sp, color = Color.Gray)
        }

    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    JeSafeTheme {
        MainScreen(rememberNavController())
    }
}
