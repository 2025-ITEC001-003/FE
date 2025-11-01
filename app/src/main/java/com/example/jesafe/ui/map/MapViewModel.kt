package com.example.jesafe.ui.map

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.local.AccidentEntity
import com.example.jesafe.data.repository.AccidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: AccidentRepository
) : ViewModel() {
    private val _accidents = mutableStateOf<List<AccidentEntity>>(emptyList())
    val accidents: State<List<AccidentEntity>> = _accidents

    fun loadAccidents() {
        viewModelScope.launch {
            _accidents.value = repository.getAllAccidents()
            Log.d("MapViewModel", "Loaded ${_accidents.value} accidents")
        }
    }
}
