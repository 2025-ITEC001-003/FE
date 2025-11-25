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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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

    private val _selectedAccident = mutableStateOf<AccidentEntity?>(null)
    val selectedAccident: State<AccidentEntity?> = _selectedAccident

    // 에러 이벤트를 위한 Channel (일회성 이벤트)
    private val _errorEvent = Channel<String>(Channel.BUFFERED)
    val errorEvent = _errorEvent.receiveAsFlow()

    // 로딩 상태
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun selectAccident(accident: AccidentEntity?) {
        _selectedAccident.value = accident
    }

    fun loadAccidents(zoom: Float, bounds: LatLngBounds? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setLoading(true)
                when {
                    zoom < ZOOM_THRESHOLD_GRID -> loadGridData()
                    zoom >= ZOOM_THRESHOLD_MARKERS -> loadSampledMarkers(bounds)
                    else -> clearAccidents()
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                setLoading(false)
            }
        }
    }

    private suspend fun loadGridData() {
        val (minDate, maxDate) = getDateRange()
        val gridList = repository.getAccidentsGroupedByGrid(minDate, maxDate)
        withContext(Dispatchers.Main) {
            _gridData.value = gridList
            _accidents.value = emptyList()
        }
    }

    private suspend fun loadSampledMarkers(bounds: LatLngBounds?) {
        val (minDate, maxDate) = getDateRange()
        val accidentList = if (bounds != null) {
            repository.getAccidentsInBoundsSampled(
                minLat = bounds.southwest.latitude,
                maxLat = bounds.northeast.latitude,
                minLon = bounds.southwest.longitude,
                maxLon = bounds.northeast.longitude,
                limit = MAX_MARKERS_DISPLAY,
                minDate = minDate,
                maxDate = maxDate
            )
        } else {
            val all = repository.getAllAccidents()
            all.filter { 
                val date = it.reportDate ?: ""
                date >= minDate && date <= maxDate 
            }.take(MAX_MARKERS_DISPLAY)
        }

        withContext(Dispatchers.Main) {
            // 선택된 마커가 리스트에 포함되도록 보장 (화면 이동 시 마커 사라짐 방지)
            val currentSelected = _selectedAccident.value
            val finalList = if (currentSelected != null) {
                if (accidentList.any { it.id == currentSelected.id }) {
                    accidentList
                } else {
                    accidentList + currentSelected
                }
            } else {
                accidentList
            }
            
            _accidents.value = finalList
            _gridData.value = emptyList()
        }
    }

    private suspend fun clearAccidents() {
        withContext(Dispatchers.Main) {
            _accidents.value = emptyList()
        }
    }

    private suspend fun setLoading(loading: Boolean) {
        withContext(Dispatchers.Main) {
            _isLoading.value = loading
        }
    }

    private fun handleError(e: Exception) {
        viewModelScope.launch {
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "네트워크 연결을 확인해주세요"
                is java.io.IOException -> "데이터를 불러오는데 실패했습니다"
                else -> "오류가 발생했습니다: ${e.message ?: "알 수 없는 오류"}"
            }
            _errorEvent.send(errorMessage)
        }
    }

    private fun getDateRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.YEAR) < 2025) {
            calendar.set(2025, Calendar.FEBRUARY, 1)
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val maxDate = dateFormat.format(calendar.time)
        
        calendar.add(Calendar.MONTH, -1)
        val minDate = dateFormat.format(calendar.time)
        
        return Pair(minDate, maxDate)
    }
}
