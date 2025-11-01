package com.example.jesafe.ui.map

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.local.AccidentEntity
import com.example.jesafe.data.local.GridCell
import com.example.jesafe.data.repository.AccidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: AccidentRepository
) : ViewModel() {
    private val _accidents = mutableStateOf<List<AccidentEntity>>(emptyList())
    val accidents: State<List<AccidentEntity>> = _accidents

    private val _gridData = mutableStateOf<List<GridCell>>(emptyList())
    val gridData: State<List<GridCell>> = _gridData

    fun loadAccidents(zoom: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                println("DEBUG: Loading accidents with zoom: $zoom") // 디버그 로그

                if (zoom < 11f) {
                    val gridList = repository.getAccidentsGroupedByGrid()
                    println("DEBUG: Grid data size: ${gridList.size}") // 디버그 로그
                    withContext(Dispatchers.Main) {
                        _gridData.value = gridList
                    }
                } else {
                    val accidentList = repository.getAllAccidents()
                    println("DEBUG: Accident data size: ${accidentList.size}") // 디버그 로그
                    withContext(Dispatchers.Main) {
                        _accidents.value = accidentList
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading accidents: ${e.message}") // 에러 로그
            }
        }
    }
}

