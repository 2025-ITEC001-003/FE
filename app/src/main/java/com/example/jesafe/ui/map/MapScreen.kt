package com.example.jesafe.ui.map

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(viewModel: MapViewModel) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(MapConstants.DEFAULT_LAT, MapConstants.DEFAULT_LON),
            MapConstants.DEFAULT_ZOOM
        )
    }

    val accidents by viewModel.accidents
    val gridData by viewModel.gridData

    LaunchedEffect(cameraPositionState.position.zoom, cameraPositionState.projection) {
        val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
        viewModel.loadAccidents(cameraPositionState.position.zoom, bounds)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        val currentZoom = cameraPositionState.position.zoom

        when {
            currentZoom < MapConstants.ZOOM_THRESHOLD_GRID -> {
                RenderHeatmapGrid(gridData)
            }

            currentZoom >= MapConstants.ZOOM_THRESHOLD_MARKERS -> {
                RenderAccidentMarkers(accidents)
            }
        }
    }
}

@Composable
private fun RenderHeatmapGrid(gridData: List<com.example.jesafe.data.local.GridCell>) {
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
    grid: com.example.jesafe.data.local.GridCell,
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
private fun RenderAccidentMarkers(accidents: List<com.example.jesafe.data.local.AccidentEntity>) {
    accidents.forEach { accident ->
        if (accident.latitude != null && accident.longitude != null) {
            AccidentMarker(accident)
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun AccidentMarker(accident: com.example.jesafe.data.local.AccidentEntity) {
    val markerColor = getMarkerColorForAccidentType(accident.accidentType)

    Marker(
        state = MarkerState(
            position = LatLng(accident.latitude!!, accident.longitude!!)
        ),
        title = accident.accidentType ?: "사고",
        snippet = formatAccidentSnippet(accident),
        icon = BitmapDescriptorFactory.defaultMarker(markerColor)
    )
}

// 상수 객체
private object MapConstants {
    const val DEFAULT_LAT = 33.38
    const val DEFAULT_LON = 126.53
    const val DEFAULT_ZOOM = 10f
    const val ZOOM_THRESHOLD_GRID = 11f
    const val ZOOM_THRESHOLD_MARKERS = 12f
    const val GRID_SIZE = 0.025
}

// 색상 계산 함수
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

// 사고 유형별 마커 색상
private fun getMarkerColorForAccidentType(accidentType: String?): Float {
    return when (accidentType?.uppercase()) {
        "차대사람" -> BitmapDescriptorFactory.HUE_RED        // 빨강 - 가장 위험
        "차대차" -> BitmapDescriptorFactory.HUE_ORANGE      // 주황
        "차량단독" -> BitmapDescriptorFactory.HUE_YELLOW    // 노랑
        "철길건널목" -> BitmapDescriptorFactory.HUE_MAGENTA  // 자주
        else -> BitmapDescriptorFactory.HUE_AZURE           // 하늘색 - 기타
    }
}

// 마커 스니펫 포맷팅
private fun formatAccidentSnippet(accident: com.example.jesafe.data.local.AccidentEntity): String {
    return buildString {
        accident.reportDate?.let { append("날짜: $it\n") }
        accident.season?.let { append("계절: $it\n") }
        accident.temperature?.let { append("기온: ${it}°C") }
    }.trim()
}

