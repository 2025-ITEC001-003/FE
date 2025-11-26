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
        // 더 이상 줌 레벨에 따라 그리드/마커를 나누지 않으므로 THRESHOLD 상수를 제거하거나 무시합니다.
        private const val MAX_MARKERS_DISPLAY = 500 
    }

    private val _accidents = mutableStateOf<List<AccidentEntity>>(emptyList())
    val accidents: State<List<AccidentEntity>> = _accidents

    // 클러스터링된 포인트 및 영역
    private val _clusteredPoints = mutableStateOf<List<ClusteredPoint>>(emptyList())
    val clusteredPoints: State<List<ClusteredPoint>> = _clusteredPoints

    private val _clusterAreas = mutableStateOf<List<ClusterArea>>(emptyList())
    val clusterAreas: State<List<ClusterArea>> = _clusterAreas

    private val _selectedAccident = mutableStateOf<AccidentEntity?>(null)
    val selectedAccident: State<AccidentEntity?> = _selectedAccident

    private val _selectedCluster = mutableStateOf<ClusterArea?>(null)
    val selectedCluster: State<ClusterArea?> = _selectedCluster

    // 에러 이벤트를 위한 Channel (일회성 이벤트)
    private val _errorEvent = Channel<String>(Channel.BUFFERED)
    val errorEvent = _errorEvent.receiveAsFlow()

    // 로딩 상태
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // 데이터가 이미 로드되었는지 확인하는 플래그
    private var isDataLoaded = false
    
    fun selectAccident(accident: AccidentEntity?) {
        _selectedAccident.value = accident
        if (accident != null) {
            _selectedCluster.value = null
        }
    }

    fun selectCluster(cluster: ClusterArea?) {
        _selectedCluster.value = cluster
        if (cluster != null) {
            _selectedAccident.value = null
        }
    }

    fun loadAccidents(zoom: Float, bounds: LatLngBounds? = null) {
        // 줌 레벨과 상관없이 최초 1회만 로드하거나, 데이터가 없을 때 로드
        // 항상 클러스터링(마커) 모드로 동작
        if (!isDataLoaded) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    setLoading(true)
                    loadAllMarkersForClustering()
                    isDataLoaded = true
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    setLoading(false)
                }
            }
        }
    }

    // 그리드 로직(loadGridData)은 사용하지 않음

    private suspend fun loadAllMarkersForClustering() {
        if (_clusteredPoints.value.isNotEmpty()) return

        val (minDate, maxDate) = getDateRange()
        
        val allAccidents = repository.getAllAccidents()
            .filter { 
                val date = it.reportDate ?: ""
                date >= minDate && date <= maxDate 
            }
            .take(MAX_MARKERS_DISPLAY)

        val (areas, points) = ClusteringHelper.performClustering(allAccidents)

        withContext(Dispatchers.Main) {
            _accidents.value = allAccidents
            _clusteredPoints.value = points
            _clusterAreas.value = areas
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
