package com.example.jesafe.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jesafe.data.local.ChatSessionEntity
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

/**
 * ViewModel for the AI Chat screen.
 *
 * This ViewModel manages the state for the chat screen, including chat sessions,
 * messages, and sending new messages. It follows MVVM architecture principles.
 *
 * @property chatRepository The repository for accessing chat data.
 */
@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    // region State
    private val _sessions = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
    /**
     * A flow of the list of chat sessions.
     */
    val sessions: StateFlow<List<ChatSessionEntity>> = _sessions.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    /**
     * A flow of the list of messages in the current chat session.
     */
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _newSessionId = MutableStateFlow<String?>(null)
    /**
     * A flow that emits the ID of a newly created session.
     */
    val newSessionId: StateFlow<String?> = _newSessionId.asStateFlow()

    private val _isSendingMessage = MutableStateFlow(false)
    /**
     * A flow that indicates whether a message is currently being sent.
     */
    val isSendingMessage: StateFlow<Boolean> = _isSendingMessage.asStateFlow()

    // endregion

    // It's recommended to use device's locale instead of hardcoding it.
    // For now, using Locale.KOREA to maintain consistency.
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    init {
        loadSessions()
    }

    /**
     * Loads all chat sessions from the repository.
     */
    fun loadSessions() {
        viewModelScope.launch {
            _sessions.value = chatRepository.getAllSessions()
        }
    }

    /**
     * Deletes a chat session and its messages.
     *
     * @param sessionId The ID of the session to delete.
     */
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            loadSessions()
        }
    }

    /**
     * Loads all messages for a specific chat session.
     *
     * @param sessionId The ID of the session to load messages for.
     */
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

    /**
     * Sends a message from the user and gets a streaming response from the AI.
     * The AI response is streamed token by token.
     *
     * Note: For storing sensitive information like API keys, use `BuildConfig` or `secrets.properties`.
     * This implementation assumes that `chatRepository` handles this securely.
     *
     * @param query The user's message.
     * @param sessionId The current session ID, or null if it's a new chat.
     */
    fun sendMessage(query: String, sessionId: String?) {
        if (query.isBlank() || _isSendingMessage.value) return

        _isSendingMessage.value = true

        val userMessage = Message(
            id = UUID.randomUUID().mostSignificantBits,
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
                    // Handle error, e.g., show an error message
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

    /**
     * Clears all messages from the current chat view.
     */
    fun clearMessages() {
        _messages.value = emptyList()
        _newSessionId.value = null
    }
}
