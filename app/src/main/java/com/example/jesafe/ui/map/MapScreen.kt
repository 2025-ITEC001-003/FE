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
import com.example.jesafe.data.local.GridCell
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
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
    val gridData by viewModel.gridData
    val isLoading by viewModel.isLoading
    val selectedAccident by viewModel.selectedAccident
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
                }
            }
        ) {
            val currentZoom = cameraPositionState.position.zoom

            when {
                currentZoom < MapConstants.ZOOM_THRESHOLD_GRID -> {
                    RenderHeatmapGrid(gridData)
                }

                currentZoom >= MapConstants.ZOOM_THRESHOLD_MARKERS -> {
                    RenderAccidentMarkers(
                        accidents = accidents,
                        selectedAccident = selectedAccident,
                        isMapAnimating = isMapAnimating,
                        isMapMoving = isMapMoving,
                        onMarkerClick = { accident ->
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
                                return@RenderAccidentMarkers
                            }

                            viewModel.selectAccident(accident)
                            scope.launch {
                                isMapAnimating = true
                                try {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLng(LatLng(accident.latitude!!, accident.longitude!!)),
                                        durationMs = 500
                                    )
                                } finally {
                                    delay(100) // 안정화 대기
                                    isMapAnimating = false
                                }
                            }
                        },
                        onInfoWindowCloseRequest = { accident ->
                            if (selectedAccident?.id == accident.id) {
                                viewModel.selectAccident(null)
                            }
                        }
                    )
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

@Composable
private fun RenderHeatmapGrid(gridData: List<GridCell>) {
    if (gridData.isEmpty()) return

    val maxCount = gridData.maxOfOrNull { it.accidentCount } ?: 1

    gridData.forEach { grid ->
        if (grid.accidentCount > 0) {
            HeatmapGridCell(grid, maxCount)
        }
    }
}

@Composable
private fun HeatmapGridCell(
    grid: GridCell,
    maxCount: Int
) {
    val intensity = (grid.accidentCount.toFloat() / maxCount).coerceIn(0f, 1f)
    val gridSize = MapConstants.GRID_SIZE

    val points = listOf(
        LatLng(grid.latCenter - gridSize, grid.lonCenter - gridSize),
        LatLng(grid.latCenter - gridSize, grid.lonCenter + gridSize),
        LatLng(grid.latCenter + gridSize, grid.lonCenter + gridSize),
        LatLng(grid.latCenter + gridSize, grid.lonCenter - gridSize)
    )

    val color = calculateHeatmapColor(intensity)

    Polygon(
        points = points,
        fillColor = color,
        strokeColor = color.copy(alpha = 0.8f),
        strokeWidth = 2f,
        clickable = false,
        geodesic = true
    )
}

@Composable
private fun RenderAccidentMarkers(
    accidents: List<AccidentEntity>,
    selectedAccident: AccidentEntity?,
    isMapAnimating: Boolean,
    isMapMoving: Boolean,
    onMarkerClick: (AccidentEntity) -> Unit,
    onInfoWindowCloseRequest: (AccidentEntity) -> Unit
) {
    accidents.forEach { accident ->
        if (accident.latitude != null && accident.longitude != null) {
            key(accident.id) {
                AccidentMarker(
                    accident = accident, 
                    isSelected = selectedAccident?.id == accident.id,
                    isMapAnimating = isMapAnimating,
                    isMapMoving = isMapMoving,
                    onMarkerClick = onMarkerClick,
                    onInfoWindowCloseRequest = onInfoWindowCloseRequest
                )
            }
        }
    }
}

@Composable
private fun AccidentMarker(
    accident: AccidentEntity,
    isSelected: Boolean,
    isMapAnimating: Boolean,
    isMapMoving: Boolean,
    onMarkerClick: (AccidentEntity) -> Unit,
    onInfoWindowCloseRequest: (AccidentEntity) -> Unit
) {
    val markerColor = getMarkerColorForAccidentType(accident.accidentType)
    val position = LatLng(accident.latitude!!, accident.longitude!!)
    val markerState = remember(accident.id) { MarkerState(position = position) }

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
        icon = BitmapDescriptorFactory.defaultMarker(markerColor),
        title = accident.accidentType ?: "사고",
        snippet = formatAccidentSnippet(accident),
        onClick = {
            onMarkerClick(accident)
            true
        },
        onInfoWindowClick = {
            onInfoWindowCloseRequest(accident)
        },
        onInfoWindowClose = {
        }
    ) { marker ->
        CustomInfoWindow(accident = accident, marker = marker)
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

private fun calculateHeatmapColor(intensity: Float): Color {
    return when {
        intensity < 0.25f -> {
            val ratio = intensity / 0.25f
            Color(
                red = (102 + (255 - 102) * ratio) / 255f,
                green = (225 + (255 - 225) * ratio) / 255f,
                blue = 0f,
                alpha = 0.5f + intensity * 0.3f
            )
        }

        intensity < 0.5f -> {
            val ratio = (intensity - 0.25f) / 0.25f
            Color(
                red = 1f,
                green = 1f - (ratio * 0.35f),
                blue = 0f,
                alpha = 0.5f + intensity * 0.3f
            )
        }

        intensity < 0.75f -> {
            val ratio = (intensity - 0.5f) / 0.25f
            Color(
                red = 1f,
                green = 0.65f - (ratio * 0.65f),
                blue = 0f,
                alpha = 0.6f + intensity * 0.3f
            )
        }

        else -> {
            Color(
                red = 1f,
                green = 0f,
                blue = 0f,
                alpha = 0.7f + intensity * 0.2f
            )
        }
    }
}

private fun getMarkerColorForAccidentType(accidentType: String?): Float {
    return when (accidentType?.uppercase()) {
        "차대사람" -> BitmapDescriptorFactory.HUE_RED
        "차대차" -> BitmapDescriptorFactory.HUE_ORANGE
        "차량단독" -> BitmapDescriptorFactory.HUE_YELLOW
        "철길건널목" -> BitmapDescriptorFactory.HUE_MAGENTA
        else -> BitmapDescriptorFactory.HUE_AZURE
    }
}

// 마커 스니펫 포맷팅
private fun formatAccidentSnippet(accident: AccidentEntity): String {
    return buildString {
        accident.reportDate?.let { append("날짜: $it\n") }
        accident.season?.let { append("계절: $it\n") }
        accident.temperature?.let { append("기온: ${it}°C") }
    }.trim()
}
