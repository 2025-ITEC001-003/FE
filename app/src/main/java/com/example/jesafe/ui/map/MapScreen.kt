package com.example.jesafe.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.jesafe.data.local.AccidentEntity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    cameraPositionState: CameraPositionState
) {
    val accidents by viewModel.accidents
    val clusteredPoints by viewModel.clusteredPoints
    val clusterAreas by viewModel.clusterAreas
    val isLoading by viewModel.isLoading
    val selectedAccident by viewModel.selectedAccident
    val selectedCluster by viewModel.selectedCluster
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isMapAnimating by remember { mutableStateOf(false) }
    val isMapMoving = cameraPositionState.isMoving

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                withDismissAction = true
            )
        }
    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .debounce(MapConstants.DEBOUNCE_DELAY_MS)
            .filter { !it }
            .collect { 
                val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                viewModel.loadAccidents(cameraPositionState.position.zoom, bounds)
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                if (!isMapAnimating && !isMapMoving) {
                    viewModel.selectAccident(null)
                    viewModel.selectCluster(null)
                }
            }
        ) {
            // 1. 클러스터 (영역 원 + 중심 핀)
            RenderClusters(
                clusterAreas = clusterAreas,
                onClusterPinClick = { cluster ->
                        viewModel.selectCluster(cluster)
                }
            )

            // 2. 개별 포인트 (점 또는 핀)
            RenderClusteredPoints(
                clusteredPoints = clusteredPoints,
                selectedAccident = selectedAccident,
                isMapAnimating = isMapAnimating,
                isMapMoving = isMapMoving,
                onPointClick = { point ->
                    val accident = point.accident
                    // 이미 선택된 상태라면 애니메이션만
                    if (selectedAccident?.id == accident.id) {
                        scope.launch {
                            isMapAnimating = true
                            try {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLng(LatLng(accident.latitude!!, accident.longitude!!)),
                                    durationMs = 500
                                )
                            } finally {
                                delay(100)
                                isMapAnimating = false
                            }
                        }
                        return@RenderClusteredPoints
                    }

                    // 선택 변경
                    viewModel.selectAccident(accident)
                    scope.launch {
                        isMapAnimating = true
                        try {
                            cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLng(LatLng(accident.latitude!!, accident.longitude!!)),
                                        durationMs = 500
                                    )
                        } finally {
                            delay(100)
                            isMapAnimating = false
                        }
                    }
                },
                onInfoWindowCloseRequest = { 
                    viewModel.selectAccident(null)
                }
            )
        }
        
        // 범례 오버레이 (우측 상단)
        if (clusterAreas.isNotEmpty()) {
            LegendOverlay(
                clusters = clusterAreas,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp),
                onClusterClick = { cluster ->
                    viewModel.selectCluster(cluster)
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(cluster.center, 14f),
                            durationMs = 800
                        )
                    }
                }
            )
        }
        
        // 클러스터 상세 정보창 (화면 중앙 오버레이)
        if (selectedCluster != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                ClusterInfoWindow(
                    cluster = selectedCluster!!,
                    onClose = { viewModel.selectCluster(null) }
                )
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

@Composable
private fun RenderClusters(
    clusterAreas: List<ClusterArea>,
    onClusterPinClick: (ClusterArea) -> Unit
) {
    clusterAreas.forEach { cluster ->
        val baseColor = ClusterUtils.getClusterColor(cluster.clusterId)
        val fillColor = baseColor.copy(alpha = 0.2f)
        val strokeColor = baseColor.copy(alpha = 0.8f)
        
        // 1. 영역 (원)
        Circle(
            center = cluster.center,
            radius = cluster.radius,
            fillColor = fillColor,
            strokeColor = strokeColor,
            strokeWidth = 2f,
            clickable = false // 영역 클릭 간섭 방지 (핀 클릭 유도)
        )

        // 2. 중심 핀 (Marker) - 정보창은 오버레이로 표시하므로 기본 MarkerInfoWindow는 사용하지 않음
        val markerState = remember(cluster.clusterId) { MarkerState(position = cluster.center) }
        
        Marker(
            state = markerState,
            icon = BitmapDescriptorFactory.defaultMarker(ClusterUtils.getMarkerHue(cluster.clusterId)),
            title = "Cluster ${cluster.clusterId}",
            onClick = {
                onClusterPinClick(cluster)
                true // 기본 InfoWindow 표시 방지
            }
        )
    }
}

@Composable
private fun RenderClusteredPoints(
    clusteredPoints: List<ClusteredPoint>,
    selectedAccident: AccidentEntity?,
    isMapAnimating: Boolean,
    isMapMoving: Boolean,
    onPointClick: (ClusteredPoint) -> Unit,
    onInfoWindowCloseRequest: (AccidentEntity) -> Unit
) {
    clusteredPoints.forEach { point ->
        if (point.accident.latitude != null && point.accident.longitude != null) {
            key(point.accident.id) {
                val isSelected = selectedAccident?.id == point.accident.id
                
                if (isSelected) {
                    // 선택된 경우: 핀(Marker)으로 상세 정보 표시
                    ClusteredAccidentMarker(
                        point = point,
                        isSelected = true,
                        isMapAnimating = isMapAnimating,
                        isMapMoving = isMapMoving,
                        onMarkerClick = onPointClick,
                        onInfoWindowCloseRequest = onInfoWindowCloseRequest
                    )
                } else {
                    // 선택되지 않은 경우: 점(Dot)으로 표시
                    // 노이즈는 회색, 클러스터는 해당 색상
                    val color = if (point.isNoise) Color.Gray else ClusterUtils.getClusterColor(point.clusterId)
                    
                    Circle(
                        center = point.position,
                        radius = 6.0, // 점의 크기 (미터 단위)
                        fillColor = color,
                        strokeColor = Color.White,
                        strokeWidth = 1f,
                        zIndex = 1.0f,
                        clickable = true,
                        onClick = { onPointClick(point) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClusteredAccidentMarker(
    point: ClusteredPoint,
    isSelected: Boolean,
    isMapAnimating: Boolean,
    isMapMoving: Boolean,
    onMarkerClick: (ClusteredPoint) -> Unit,
    onInfoWindowCloseRequest: (AccidentEntity) -> Unit
) {
    val markerHue = ClusterUtils.getMarkerHue(point.clusterId)
    val zIndex = 2.0f // 선택된 마커는 점보다 위에
    
    val position = point.position
    val markerState = remember(point.accident.id) { MarkerState(position = position) }

    LaunchedEffect(isSelected, isMapAnimating, isMapMoving) {
        if (isSelected) {
            if (!isMapAnimating && !isMapMoving) {
                delay(50)
                markerState.showInfoWindow()
            }
        } else {
            markerState.hideInfoWindow()
        }
    }

    MarkerInfoWindow(
        state = markerState,
        icon = BitmapDescriptorFactory.defaultMarker(markerHue),
        zIndex = zIndex,
        title = point.accident.accidentType ?: "사고",
        snippet = formatAccidentSnippet(point.accident),
        onClick = {
            onMarkerClick(point)
            true
        },
        onInfoWindowClick = {
            onInfoWindowCloseRequest(point.accident)
        },
        onInfoWindowClose = {
             onInfoWindowCloseRequest(point.accident)
        }
    ) { marker ->
        CustomInfoWindow(accident = point.accident, onClose = { onInfoWindowCloseRequest(point.accident) })
    }
}

object MapConstants {
    const val DEFAULT_LAT = 33.38
    const val DEFAULT_LON = 126.53
    const val DEFAULT_ZOOM = 10f
    const val ZOOM_THRESHOLD_GRID = 11f
    const val ZOOM_THRESHOLD_MARKERS = 12f
    const val GRID_SIZE = 0.025
    const val DEBOUNCE_DELAY_MS = 500L
}

// 마커 스니펫 포맷팅
private fun formatAccidentSnippet(accident: AccidentEntity): String {
    return buildString {
        accident.reportDate?.let { append("날짜: $it\n") }
        accident.season?.let { append("계절: $it\n") }
        accident.temperature?.let { append("기온: ${it}°C") }
    }.trim()
}
