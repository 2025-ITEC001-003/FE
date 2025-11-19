package com.example.jesafe.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object LoadingState {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun show() {
        _isLoading.value = true
    }

    fun hide() {
        _isLoading.value = false
    }
}
