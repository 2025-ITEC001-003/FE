package com.example.jesafe.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.datastore.UserPreferencesRepository
import com.example.jesafe.data.model.UpdateUserRequest
import com.example.jesafe.data.repository.AuthRepository
import com.example.jesafe.data.repository.UserRepository
import com.example.jesafe.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyInfoViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val user: StateFlow<User?> = userRepository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * 변경된 사용자 정보를 서버 및 로컬에 업데이트합니다.
     * null로 전달된 값은 기존 값을 유지합니다.
     */
    fun updateUserInfo(
        newNickname: String? = null,
        newPhone: String? = null,
        newEmail: String? = null,
        newDiseaseInfo: String? = null
    ) {
        viewModelScope.launch {
            val currentUser = user.value ?: return@launch
            val token = userPreferencesRepository.accessToken.first()

            if (token.isNullOrBlank()) {
                Log.e("MyInfoViewModel", "토큰 없음")
                return@launch
            }

            // 기저질환 정보가 빈 문자열이면 null로 처리 (DB 삭제)
            val processedDiseaseInfo = if (newDiseaseInfo == "") null else (newDiseaseInfo ?: currentUser.diseaseInfo)

            // 변경할 값만 새 값으로 대체하고, 나머지는 기존 값 유지
            val request = UpdateUserRequest(
                nickname = newNickname ?: currentUser.nickname,
                birthYear = currentUser.birthYear?.toString() ?: "",
                gender = currentUser.gender ?: "",
                email = newEmail ?: currentUser.email,
                phone = newPhone ?: currentUser.phone,
                diseaseInfo = processedDiseaseInfo
            )

            Log.d("MyInfoViewModel", "정보 업데이트 요청: $request")

            try {
                val response = authRepository.updateUserInfo(token, request)

                if (response.isSuccessful) {
                    // 서버 성공 시 로컬 데이터도 갱신
                    val updatedUser = currentUser.copy(
                        nickname = request.nickname ?: currentUser.nickname,
                        email = request.email,
                        phone = request.phone,
                        diseaseInfo = request.diseaseInfo
                    )
                    userRepository.saveUser(updatedUser)
                    Log.d("MyInfoViewModel", "정보 업데이트 성공")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "알 수 없는 오류"
                    Log.e("MyInfoViewModel", "정보 업데이트 실패: ${response.code()} / $errorBody")
                }
            } catch (e: Exception) {
                Log.e("MyInfoViewModel", "네트워크 오류", e)
            }
        }
    }
}
