package com.example.jesafe.ui.map

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.local.AccidentEntity
import com.example.jesafe.data.local.GridCell
import com.example.jesafe.data.repository.AccidentRepository
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: AccidentRepository
) : ViewModel() {

    companion object {
        private const val ZOOM_THRESHOLD_GRID = 11f
        private const val ZOOM_THRESHOLD_MARKERS = 12f
        private const val MAX_MARKERS_DISPLAY = 150
    }

    private val _accidents = mutableStateOf<List<AccidentEntity>>(emptyList())
    val accidents: State<List<AccidentEntity>> = _accidents

    private val _gridData = mutableStateOf<List<GridCell>>(emptyList())
    val gridData: State<List<GridCell>> = _gridData

    fun loadAccidents(zoom: Float, bounds: LatLngBounds? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when {
                    zoom < ZOOM_THRESHOLD_GRID -> loadGridData()
                    zoom >= ZOOM_THRESHOLD_MARKERS -> loadSampledMarkers(bounds)
                    else -> clearAccidents()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun loadGridData() {
        val gridList = repository.getAccidentsGroupedByGrid()
        withContext(Dispatchers.Main) {
            _gridData.value = gridList
            _accidents.value = emptyList()
        }
    }

    private suspend fun loadSampledMarkers(bounds: LatLngBounds?) {
        val accidentList = if (bounds != null) {
            repository.getAccidentsInBoundsSampled(
                minLat = bounds.southwest.latitude,
                maxLat = bounds.northeast.latitude,
                minLon = bounds.southwest.longitude,
                maxLon = bounds.northeast.longitude,
                limit = MAX_MARKERS_DISPLAY
            )
        } else {
            repository.getAllAccidents().take(MAX_MARKERS_DISPLAY)
        }

        withContext(Dispatchers.Main) {
            _accidents.value = accidentList
            _gridData.value = emptyList()
        }
    }

    private suspend fun clearAccidents() {
        withContext(Dispatchers.Main) {
            _accidents.value = emptyList()
        }
    }

    private fun handleError(e: Exception) {
        // TODO: 에러 처리 로직 추가 (예: LiveData로 에러 상태 전달)
        println("Error loading accidents: ${e.message}")
    }
}
