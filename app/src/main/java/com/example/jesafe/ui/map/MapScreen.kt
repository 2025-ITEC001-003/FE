package com.example.jesafe.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Composable
fun MapScreen(viewModel: MapViewModel) {
    val context = LocalContext.current
    val accidents by viewModel.accidents
    val gridData by viewModel.gridData
    val isLoading by viewModel.isLoading
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // MapView 상태 관리
    val mapView = remember { MapView(context) }
    val googleMap = remember { androidx.compose.runtime.mutableStateOf<GoogleMap?>(null) }
    val clusterManager = remember {
        androidx.compose.runtime.mutableStateOf<ClusterManager<AccidentClusterItem>?>(null)
    }
    val currentZoom =
        remember { androidx.compose.runtime.mutableFloatStateOf(MapConstants.DEFAULT_ZOOM) }

    // 에러 이벤트 수신 및 Snackbar 표시
    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                withDismissAction = true
            )
        }
    }

    // MapView 생명주기 관리
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            view.getMapAsync { map ->
                if (googleMap.value == null) {
                    googleMap.value = map

                    // 지도 UI 설정
                    map.uiSettings.apply {
                        isZoomControlsEnabled = true
                        isZoomGesturesEnabled = true
                        isScrollGesturesEnabled = true
                        isRotateGesturesEnabled = true
                        isTiltGesturesEnabled = true
                        isCompassEnabled = true
                        isMyLocationButtonEnabled = false
                    }

                    // 초기 카메라 위치 설정
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(MapConstants.DEFAULT_LAT, MapConstants.DEFAULT_LON),
                            MapConstants.DEFAULT_ZOOM
                        )
                    )

                    // 클러스터 매니저 초기화
                    val manager = ClusterManager<AccidentClusterItem>(context, map)
                    clusterManager.value = manager

                    // 커스텀 렌더러 설정
                    AccidentClusterRenderer(context, map, manager)

                    // 지도 이벤트를 클러스터 매니저에 위임
                    map.setOnCameraIdleListener {
                        manager.onCameraIdle()

                        // 줌 레벨과 화면 영역 가져오기
                        val zoom = map.cameraPosition.zoom
                        currentZoom.floatValue = zoom
                        val bounds = map.projection.visibleRegion.latLngBounds

                        // 데이터 로드
                        coroutineScope.launch {
                            viewModel.loadAccidents(zoom, bounds)
                        }
                    }

                    map.setOnMarkerClickListener(manager)
                    map.setInfoWindowAdapter(manager.markerManager)
                    map.setOnInfoWindowClickListener(manager)

                    // 초기 데이터 로드
                    coroutineScope.launch {
                        viewModel.loadAccidents(MapConstants.DEFAULT_ZOOM, null)
                    }
                }
            }
        }

        // 데이터 변경 시 마커 업데이트
        LaunchedEffect(accidents, gridData, currentZoom.floatValue) {
            clusterManager.value?.let { manager ->
                manager.clearItems()

                when {
                    // 줌 레벨 12 이상: 클러스터링된 마커 표시
                    currentZoom.floatValue >= MapConstants.ZOOM_THRESHOLD_MARKERS -> {
                        accidents.forEach { accident ->
                            if (accident.latitude != null && accident.longitude != null) {
                                val item = AccidentClusterItem(accident)
                                manager.addItem(item)
                            }
                        }
                        manager.cluster()
                    }
                    // 줌 레벨 10-12: 그리드 히트맵 (별도 처리 필요)
                    // 여기서는 클러스터링 마커만 처리
                }
            }
        }

        // 로딩 인디케이터
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        // Snackbar 호스트
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

// 상수 객체
private object MapConstants {
    const val DEFAULT_LAT = 33.38
    const val DEFAULT_LON = 126.53
    const val DEFAULT_ZOOM = 10f
    const val ZOOM_THRESHOLD_GRID = 11f
    const val ZOOM_THRESHOLD_MARKERS = 12f
    const val GRID_SIZE = 0.025
    const val DEBOUNCE_DELAY_MS = 500L
}
