package com.example.jesafe.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.network.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

/**
 * AI 채팅 화면의 뷰모델
 *
 * 사용자 메시지 전송과 AI 응답 수신을 관리합니다.
 * 스트리밍 방식으로 받은 응답을 실시간으로 UI에 반영합니다.
 *
 * @property chatRepository 채팅 저장소
 */
@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val sessionId = UUID.randomUUID().toString()
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    /**
     * 사용자 메시지를 전송하고 AI 응답을 받습니다.
     *
     * 1. 사용자 메시지를 목록에 추가
     * 2. 로딩 메시지("...") 표시
     * 3. 서버로부터 스트리밍 응답 수신
     * 4. 응답을 실시간으로 업데이트
     *
     * @param query 사용자의 질문 텍스트
     */
    fun sendMessage(query: String) {
        if (query.isBlank()) return

        val currentTime = timeFormat.format(Date())
        val userMessage = Message(System.currentTimeMillis(), query, true, currentTime)
        val thinkingMessage = Message(System.currentTimeMillis() + 1, "...", false, "")

        _messages.value = _messages.value + userMessage + thinkingMessage

        viewModelScope.launch {
            val responseBuilder = StringBuilder()

            try {
                chatRepository.sendChatMessage(query, sessionId).collect { chunk ->
                    responseBuilder.append(chunk)

                    val aiResponse = Message(
                        id = thinkingMessage.id,
                        text = responseBuilder.toString(),
                        isUser = false,
                        timestamp = timeFormat.format(Date())
                    )

                    _messages.value = _messages.value.map {
                        if (it.id == thinkingMessage.id) aiResponse else it
                    }
                }
            } catch (e: Exception) {
                val errorMessage = Message(
                    id = thinkingMessage.id,
                    text = "죄송합니다. 응답을 받는 중 오류가 발생했습니다.",
                    isUser = false,
                    timestamp = timeFormat.format(Date())
                )
                _messages.value = _messages.value.map {
                    if (it.id == thinkingMessage.id) errorMessage else it
                }
            }
        }
    }
}

