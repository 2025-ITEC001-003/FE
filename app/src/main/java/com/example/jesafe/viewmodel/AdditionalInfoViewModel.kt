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

    // UI 상태 (로딩, 성공, 실패)
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Initial)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    // 사용자 이름
    private val _userName = MutableStateFlow<String>("사용자")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // 1단계 정보
    private val _birthYear = MutableStateFlow<String?>(null)
    private val _gender = MutableStateFlow<String?>(null)

    // 2단계 정보
    private val _email = MutableStateFlow<String?>(null)
    private val _phone = MutableStateFlow<String?>(null)

    /**
     * 화면 진입 시 사용자 이름을 설정합니다.
     */
    fun setUserName(name: String) {
        _userName.value = name
    }

    /**
     * 1단계 (성별, 출생년도) 정보를 ViewModel에 저장합니다.
     */
    fun saveBirthAndGender(birthYear: String, gender: String) {
        _birthYear.value = birthYear
        _gender.value = gender
    }

    /**
     * 2단계 (이메일, 전화번호) 정보를 ViewModel에 저장합니다.
     */
    fun saveContact(email: String, phone: String) {
        _email.value = email
        _phone.value = phone
    }

    /**
     * 3단계 (기저질환) 정보를 저장하고, 취합된 모든 정보로 서버에 회원가입(정보 업데이트)을 요청합니다.
     */
    fun saveDiseaseAndRegister(diseaseInfo: String?) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val token = userPreferencesRepository.accessToken.first()
                if (token.isNullOrBlank()) {
                    _updateState.value = UpdateState.Error("인증 토큰이 없습니다. 다시 로그인해주세요.")
                    return@launch
                }

                // ViewModel에 저장된 모든 정보를 사용하여 요청 객체 생성
                val request = UpdateUserRequest(
                    birthYear = _birthYear.value ?: "",
                    gender = _gender.value ?: "",
                    email = _email.value ?: "",
                    phone = _phone.value ?: "",
                    diseaseInfo = diseaseInfo ?: ""
                )

                val response = authRepository.updateUserInfo(token, request)
                if (response.isSuccessful) {
                    // --- 로컬 데이터 업데이트 ---
                    val currentUser = userRepository.getUser().first()
                    currentUser?.let {
                        val updatedUser = it.copy(
                            birthYear = _birthYear.value?.toIntOrNull(),
                            gender = _gender.value,
                            email = _email.value,
                            phone = _phone.value,
                            diseaseInfo = diseaseInfo
                        )
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
