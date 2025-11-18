package com.example.jesafe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.model.UpdateUserRequest
import com.example.jesafe.data.repository.AuthRepository
import com.example.jesafe.data.datastore.UserPreferencesRepository
import com.example.jesafe.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UpdateState {
    object Initial : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

@HiltViewModel
class AdditionalInfoViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun saveAdditionalInfo(birthYear: String, gender: String, diseaseInfo: String?) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val token = userPreferencesRepository.accessToken.first()
                if (token.isNullOrBlank()) {
                    _updateState.value = UpdateState.Error("인증 토큰이 없습니다. 다시 로그인해주세요.")
                    return@launch
                }

                val request = UpdateUserRequest(
                    birthYear = birthYear,
                    gender = gender,
                    diseaseInfo = diseaseInfo
                )

                val response = authRepository.updateUserInfo(token, request)
                if (response.isSuccessful) {
                    // --- 로컬 데이터 업데이트 로직 추가 ---
                    // 1. 기존 정보를 불러옵니다.
                    val currentUser = userRepository.getUser().first()

                    // 2. 새로운 정보로 덮어쓰지 않고, copy를 사용하여 필요한 부분만 업데이트합니다.
                    currentUser?.let {
                        val updatedUser = it.copy(
                            birthYear = birthYear.toIntOrNull(),
                            gender = gender,
                            diseaseInfo = diseaseInfo
                        )
                        // 3. 업데이트된 정보를 로컬에 저장합니다.
                        userRepository.saveUser(updatedUser)
                    }

                    _updateState.value = UpdateState.Success
                } else {
                    val errorBody = response.errorBody()?.string() ?: "알 수 없는 오류"
                    _updateState.value = UpdateState.Error("정보 저장에 실패했습니다: ${response.code()} / $errorBody")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("네트워크 오류가 발생했습니다: ${e.message}")
            }
        }
    }
}
