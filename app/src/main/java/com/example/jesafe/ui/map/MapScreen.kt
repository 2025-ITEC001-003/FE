package com.example.jesafe.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(viewModel: MapViewModel) {
    val accidents by viewModel.accidents

    val cameraPositionState = rememberCameraPositionState {
        // 초기 위치 (제주도 중심)
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
            LatLng(33.38, 126.53), 9f
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadAccidents()
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        accidents.forEach { accident ->
            if (accident.latitude != null && accident.longitude != null) {
                Marker(
                    state = MarkerState(position = LatLng(accident.latitude, accident.longitude)),
                    title = accident.accidentType ?: "사고",
                    snippet = "${accident.season ?: ""} / ${accident.reportWeekday ?: ""}"
                )
            }
        }
    }
}