package com.example.jesafe.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun MapScreen(viewModel: MapViewModel) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(33.38, 126.53), 10f)
    }

    val accidents by viewModel.accidents
    val gridData by viewModel.gridData

    LaunchedEffect(cameraPositionState.position.zoom) {
        viewModel.loadAccidents(cameraPositionState.position.zoom)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapLoaded = { viewModel.loadAccidents(cameraPositionState.position.zoom) }
    ) {

        val currentZoom = cameraPositionState.position.zoom

        // 줌 레벨 10-12: 그리드 기반 직사각형 히트맵
        if (currentZoom in 10f..12f && gridData.isNotEmpty()) {
            val maxCount = gridData.maxOfOrNull { it.accidentCount } ?: 1

            gridData.forEach { grid ->
                if (grid.accidentCount > 0) {
                    val intensity = (grid.accidentCount.toFloat() / maxCount).coerceIn(0f, 1f)

                    // 그리드 크기 (약 0.05도 = 약 5km)
                    val gridSize = 0.025

                    // 직사각형 꼭지점 계산
                    val points = listOf(
                        LatLng(grid.latCenter - gridSize, grid.lonCenter - gridSize), // 좌하단
                        LatLng(grid.latCenter - gridSize, grid.lonCenter + gridSize), // 우하단
                        LatLng(grid.latCenter + gridSize, grid.lonCenter + gridSize), // 우상단
                        LatLng(grid.latCenter + gridSize, grid.lonCenter - gridSize)  // 좌상단
                    )

                    // 강도에 따른 색상 계산 (녹색 -> 노랑 -> 주황 -> 빨강)
                    val color = when {
                        intensity < 0.25f -> {
                            // 녹색 영역
                            val ratio = intensity / 0.25f
                            ComposeColor(
                                red = (102 + (255 - 102) * ratio) / 255f,
                                green = (225 + (255 - 225) * ratio) / 255f,
                                blue = (0f),
                                alpha = 0.5f + intensity * 0.3f
                            )
                        }

                        intensity < 0.5f -> {
                            // 노랑 영역
                            val ratio = (intensity - 0.25f) / 0.25f
                            ComposeColor(
                                red = 1f,
                                green = 1f - (ratio * 0.35f),
                                blue = 0f,
                                alpha = 0.5f + intensity * 0.3f
                            )
                        }

                        intensity < 0.75f -> {
                            // 주황 영역
                            val ratio = (intensity - 0.5f) / 0.25f
                            ComposeColor(
                                red = 1f,
                                green = 0.65f - (ratio * 0.65f),
                                blue = 0f,
                                alpha = 0.6f + intensity * 0.3f
                            )
                        }

                        else -> {
                            // 빨강 영역
                            ComposeColor(
                                red = 1f,
                                green = 0f,
                                blue = 0f,
                                alpha = 0.7f + intensity * 0.2f
                            )
                        }
                    }

                    Polygon(
                        points = points,
                        fillColor = color,
                        strokeColor = color.copy(alpha = 0.8f),
                        strokeWidth = 2f,
                        clickable = false,
                        geodesic = true
                    )
                }
            }
        }

        // 줌 레벨 12 이상: 개별 마커
        if (currentZoom >= 12f) {
            accidents.forEach { accident ->
                if (accident.latitude != null && accident.longitude != null) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(accident.latitude, accident.longitude)
                        ),
                        title = accident.accidentType ?: "사고"
                    )
                }
            }
        }
    }
}