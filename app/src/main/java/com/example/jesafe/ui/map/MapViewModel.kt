package com.example.jesafe.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _accidents = MutableLiveData<List<AccidentEntity>>()
    val accidents: LiveData<List<AccidentEntity>> = _accidents

    fun loadAccidents() {
        viewModelScope.launch {
            _accidents.value = repository.getAllAccidents()
        }
    }
}