package com.example.jesafe.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.local.ChatSessionEntity
import com.example.jesafe.data.network.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
    val sessions: StateFlow<List<ChatSessionEntity>> = _sessions.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _newSessionId = MutableStateFlow<String?>(null)
    val newSessionId: StateFlow<String?> = _newSessionId.asStateFlow()

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _sessions.value = chatRepository.getAllSessions()
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            loadSessions()
        }
    }

    fun loadMessagesForSession(sessionId: String) {
        viewModelScope.launch {
            val history = chatRepository.getMessagesForSession(sessionId).map {
                Message(
                    id = it.id.toLong(),
                    text = it.message,
                    isUser = it.isUser,
                    timestamp = timeFormat.format(Date(it.timestamp))
                )
            }
            _messages.value = history
        }
    }

    fun sendMessage(query: String, sessionId: String?) {
        if (query.isBlank()) return

        val userMessage = Message(
            id = System.currentTimeMillis(),
            text = query,
            isUser = true,
            timestamp = timeFormat.format(Date())
        )
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            val isNewChat = sessionId == null
            val currentSessionId = sessionId ?: UUID.randomUUID().toString()

            if (isNewChat) {
                _newSessionId.value = currentSessionId
            }

            chatRepository.sendChatMessage(query, currentSessionId).collect()

            // 스트림이 완료된 후 DB에서 전체 메시지 목록을 다시 로드
            loadMessagesForSession(currentSessionId)
            // 세션 목록도 새로고침
            loadSessions()
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
}
