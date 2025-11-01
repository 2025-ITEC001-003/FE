package com.example.jesafe.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.heatmaps.HeatmapTileProvider

@Composable
fun MapScreen(viewModel: MapViewModel) {
    // 1️⃣ 카메라 초기값 - 제주도 중심
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(33.38, 126.53), 10f // 제주도 중심 + 줌 기본값
        )
    }

    // 2️⃣ DB에서 사고 데이터 불러오기
    val accidents by viewModel.accidents

    // 3️⃣ 지도 객체 상태
    val context = LocalContext.current
    var map by remember { mutableStateOf<GoogleMap?>(null) }

    // 4️⃣ 지도 컴포저블
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,

        ) {
        // 5️⃣ 사고 데이터 → Heatmap 으로 변환
        val accidentPoints = remember(accidents) {
            accidents.mapNotNull { accident ->
                if (accident.latitude != null && accident.longitude != null)
                    LatLng(accident.latitude, accident.longitude)
                else null
            }
        }

        // 6️⃣ Heatmap 생성 및 지도에 Overlay 추가
        LaunchedEffect(accidentPoints) {
            map?.let { googleMap ->
                googleMap.clear() // 기존 레이어 제거
                if (accidentPoints.isNotEmpty()) {
                    val provider = HeatmapTileProvider.Builder()
                        .data(accidentPoints)
                        .radius(30)         // 점 번짐 반경 (조정 가능)
                        .opacity(0.7)       // 투명도
                        .build()
                    googleMap.addTileOverlay(TileOverlayOptions().tileProvider(provider))
                }
            }
        }
    }
}
