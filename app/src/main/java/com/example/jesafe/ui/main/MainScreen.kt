package com.example.jesafe.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.R
import com.example.jesafe.data.local.AccidentDao
import com.example.jesafe.data.local.AccidentEntity
import com.example.jesafe.data.local.GridCell
import com.example.jesafe.data.repository.AccidentRepository
import com.example.jesafe.ui.map.MapConstants
import com.example.jesafe.ui.map.MapScreen
import com.example.jesafe.ui.map.MapViewModel
import com.example.jesafe.ui.theme.JeSafeTheme
import kotlinx.coroutines.delay
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

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
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route || (screen.route == "chat_history" && currentDestination.route == "new_chat") } == true
                    
                    NavigationBarItem(
                        icon = { Icon(painterResource(id = screen.resourceId), contentDescription = screen.title, modifier = Modifier.size(24.dp)) },
                        label = { Text(screen.title, fontSize = 11.sp) },
                        selected = isSelected,
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
                val mapViewModel: MapViewModel = hiltViewModel()
                HomeScreen(mapViewModel = mapViewModel)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A284D))
        )
        TopContent(navController = navController)
fun HomeScreen(mapViewModel: MapViewModel) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(MapConstants.DEFAULT_LAT, MapConstants.DEFAULT_LON),
            MapConstants.DEFAULT_ZOOM
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapScreen(
            viewModel = mapViewModel,
            cameraPositionState = cameraPositionState
        )
        TopContent()
        MapControlButtons(cameraPositionState = cameraPositionState)
        BottomInfoSheet()
    }
}

@Composable
fun TopContent(navController: NavController) {
fun BoxScope.MapControlButtons(cameraPositionState: CameraPositionState) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FloatingActionButton(
            onClick = { /* TODO: 현재 위치로 이동 */ },
            shape = CircleShape,
            containerColor = Color.White,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_my_location),
                contentDescription = "Current Location",
                tint = Color(0xFF2CA0F3),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        FloatingActionButton(
            onClick = {
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 500)
                }
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = Color.White,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Zoom In",
                tint = Color.DarkGray,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        FloatingActionButton(
            onClick = {
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 500)
                }
            },
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            containerColor = Color.White,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_remove),
                contentDescription = "Zoom Out",
                tint = Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )
        }
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
    var chatRooms by remember { mutableStateOf(listOf<ChatRoom>()) }
    var isLoading by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(0) }
    val itemsPerPage = 10

    suspend fun loadMoreItems() {
        if (isLoading) return
        isLoading = true
        delay(1000) // Simulate network delay
        val newItems = List(itemsPerPage) { i ->
            val id = page * itemsPerPage + i
            ChatRoom(id, "제주도 동쪽 여행 계획 ${id + 1}", "네, 좋습니다. 추가로...", "오후 3:4${id % 10}")
        }
        chatRooms = chatRooms + newItems
        page++
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadMoreItems()
    }

    val listState = rememberLazyListState()

    val endOfListReached by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                false
            } else {
                visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1
            }
        }
    }

    LaunchedEffect(endOfListReached) {
        if (endOfListReached) {
            loadMoreItems()
        }
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
            state = listState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatRooms) { room ->
                ChatHistoryItem(room)
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
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
fun HomeScreenPreview() {
    val fakeDao = object : AccidentDao {
        override suspend fun getAll(): List<AccidentEntity> = emptyList()
        override suspend fun getAccidentsGroupedByGrid(): List<GridCell> = emptyList()
        override suspend fun getAccidentsInBoundsSampled(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double, limit: Int): List<AccidentEntity> = emptyList()
        override suspend fun insertAll(accidents: List<AccidentEntity>) {}
    }
    val fakeRepository = AccidentRepository(fakeDao)
    val previewViewModel = remember { MapViewModel(fakeRepository) }

    JeSafeTheme {
        HomeScreen(mapViewModel = previewViewModel)
    }
}
