package com.example.jesafe.ui.main

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.jesafe.R
import com.example.jesafe.data.local.AccidentDao
import com.example.jesafe.data.local.AccidentEntity
import com.example.jesafe.data.local.GridCell
import com.example.jesafe.data.repository.AccidentRepository
import com.example.jesafe.ui.auth.LoginViewModel
import com.example.jesafe.ui.chatbot.AiChatScreen
import com.example.jesafe.ui.chatbot.ChatDetailScreen
import com.example.jesafe.ui.map.MapConstants
import com.example.jesafe.ui.map.MapScreen
import com.example.jesafe.ui.map.MapViewModel
import com.example.jesafe.ui.main.MyInfoScreen
import com.example.jesafe.ui.theme.JeSafeTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class BottomNavItem(val route: String, val resourceId: Int, val title: String) {
    object Home : BottomNavItem("home", R.drawable.ic_home, "홈")
    object AiChat : BottomNavItem("ai_chat", R.drawable.ic_chat, "AI 채팅")
    object MyInfo : BottomNavItem("my_info", R.drawable.ic_my_info, "내 정보")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(rootNavController: NavController, loginViewModel: LoginViewModel) {
    val bottomNavItems = listOf(BottomNavItem.Home, BottomNavItem.AiChat, BottomNavItem.MyInfo)

    val pagerState = rememberPagerState { bottomNavItems.size }
    val coroutineScope = rememberCoroutineScope()

    val mapViewModel: MapViewModel = hiltViewModel()
    val chatNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.shadow(
                    elevation = 12.dp,
                    spotColor = Color.Black,
                    ambientColor = Color.Black
                ),
                containerColor = Color.White
            ) {
                bottomNavItems.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(id = screen.resourceId),
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(screen.title, fontSize = 11.sp) },
                        selected = pagerState.currentPage == index,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2CA0F3),
                            selectedTextColor = Color(0xFF2CA0F3),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
            userScrollEnabled = pagerState.currentPage != 0
        ) { page ->
            when (page) {
                0 -> {
                    HomeScreen(mapViewModel = mapViewModel)
                }
                1 -> {
                    NavHost(
                        navController = chatNavController,
                        startDestination = "ai_chat_list"
                    ) {
                        composable("ai_chat_list") {
                            AiChatScreen(navController = chatNavController)
                        }
                        composable(
                            route = "chatDetail/{sessionId}",
                            arguments = listOf(navArgument("sessionId") { nullable = true }),
                            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getString("sessionId")
                            ChatDetailScreen(sessionId = sessionId, navController = chatNavController)
                        }
                    }
                }
                2 -> {
                    MyInfoScreen(
                        rootNavController = rootNavController,
                        loginViewModel = loginViewModel
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mapViewModel: MapViewModel) {
    var showWarning by remember { mutableStateOf(true) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(MapConstants.DEFAULT_LAT, MapConstants.DEFAULT_LON),
            MapConstants.DEFAULT_ZOOM
        )
    }

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 100.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = Color.White,
        sheetContent = {
            BottomInfoContent()
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MapScreen(
                viewModel = mapViewModel,
                cameraPositionState = cameraPositionState
            )

            TopContent(showWarning = showWarning, onDismissWarning = { showWarning = false })

            MapControlButtons(cameraPositionState = cameraPositionState)
        }
    }
}

@Composable
fun BottomInfoContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .heightIn(min = 20.dp, max = 500.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                "현재 위치 안전도",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(
                    "안전",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            InfoColumn(value = "92", label = "안전지수", color = Color(0xFF2CA0F3))
            InfoColumn(value = "3", label = "근처 위험", color = Color.Gray)
            InfoColumn(value = "27", label = "온도", color = Color.Gray)
        }
        Spacer(Modifier.height(24.dp))

        Text("상세 정보 더보기...", color = Color.Gray, modifier = Modifier.padding(vertical = 20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(id = R.drawable.ic_clock),
                contentDescription = "Update",
                tint = Color.Gray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("마지막 업데이트: 2분 전", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun InfoColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun BoxScope.MapControlButtons(cameraPositionState: CameraPositionState) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            scope.launch {
                try {
                    val cancellationTokenSource = CancellationTokenSource()
                    val location = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).await()

                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f),
                            1000
                        )
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(180.dp))

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
        
        Spacer(modifier = Modifier.height(16.dp))

        FloatingActionButton(
            onClick = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
             modifier = Modifier.size(48.dp),
             shape = CircleShape,
             containerColor = Color.White
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_my_location),
                contentDescription = "Current Location",
                tint = Color(0xFF2CA0F3),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TopContent(showWarning: Boolean, onDismissWarning: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (showWarning) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_warning),
                        contentDescription = "Warning",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "현재 서귀포시 사고 다발 경보 발령",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissWarning, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "안전",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_people),
                        contentDescription = "Users",
                        tint = Color(0xFF2CA0F3),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "127명 접속중",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
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
            Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        "현재 위치 안전도",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(
                            "안전",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "92",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2CA0F3)
                        )
                        Text("안전지수", fontSize = 12.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "3",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text("근처 위험", fontSize = 12.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "27",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text("온도", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(id = R.drawable.ic_clock),
                        contentDescription = "Update",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("마지막 업데이트: 2분 전", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val fakeDao = object : AccidentDao {
        override suspend fun getAll(): List<AccidentEntity> = emptyList()
        
        override suspend fun getAccidentsGroupedByGrid(minDate: String, maxDate: String): List<GridCell> = emptyList()
        
        override suspend fun getAccidentsInBoundsSampled(
            minLat: Double,
            maxLat: Double,
            minLon: Double,
            maxLon: Double,
            limit: Int,
            minDate: String,
            maxDate: String
        ): List<AccidentEntity> = emptyList()

        override suspend fun insertAll(accidents: List<AccidentEntity>) {}

        override suspend fun deleteAll() {}
    }
    val fakeRepository = AccidentRepository(fakeDao)
    val previewViewModel = remember { MapViewModel(fakeRepository) }

    JeSafeTheme {
        HomeScreen(mapViewModel = previewViewModel)
    }
}