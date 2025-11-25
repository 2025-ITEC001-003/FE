package com.example.jesafe.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.local.ChatSessionWithPreview
import com.example.jesafe.data.network.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
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

    private val _sessions = MutableStateFlow<List<ChatSessionWithPreview>>(emptyList())
    val sessions: StateFlow<List<ChatSessionWithPreview>> = _sessions.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _newSessionId = MutableStateFlow<String?>(null)
    val newSessionId: StateFlow<String?> = _newSessionId.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _sessions.value = chatRepository.getSessionsWithLastMessage()
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            loadSessions()
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
            _messages.value = _messages.value.filterNot { it.id == messageId }
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
        if (query.isBlank() || _isSendingMessage.value) return

        _isSendingMessage.value = true

        viewModelScope.launch {
            val userMessage = Message(
                id = UUID.randomUUID().mostSignificantBits,
                text = query,
                isUser = true,
                timestamp = timeFormat.format(Date())
            )
            _messages.value = _messages.value + userMessage

            val isNewChat = sessionId == null
            val currentSessionId = sessionId ?: UUID.randomUUID().toString()

            if (isNewChat) {
                _newSessionId.value = currentSessionId
            }

            val aiMessageId = UUID.randomUUID().mostSignificantBits
            val aiMessage = Message(
                id = aiMessageId,
                text = "",
                isUser = false,
                timestamp = timeFormat.format(Date())
            )
            _messages.value = _messages.value + aiMessage

            var accumulatedResponse = ""
            chatRepository.sendChatMessage(query, currentSessionId)
                .catch { e ->
                    val errorMessage = "Error: ${e.message}"
                    _messages.value = _messages.value.map {
                        if (it.id == aiMessageId) it.copy(text = errorMessage) else it
                    }
                }
                .onCompletion {
                    _isSendingMessage.value = false
                    if (isNewChat) {
                        loadSessions()
                    }
                }
                .collect { chunk ->
                    accumulatedResponse += chunk
                    _messages.value = _messages.value.map {
                        if (it.id == aiMessageId) it.copy(text = accumulatedResponse) else it
                    }
                }
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
        _newSessionId.value = null
    }
}
