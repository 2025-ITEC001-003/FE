package com.example.jesafe.data.network

import android.util.Log
import com.example.jesafe.data.local.ChatDao
import com.example.jesafe.data.local.ChatEntity
import com.example.jesafe.data.local.ChatSessionWithPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ChatApiService,
    private val chatDao: ChatDao
) {
    companion object {
        private const val TAG = "ChatRepository"
        private const val CHUNK_KEY = "chunk"
        private const val ERROR_MESSAGE = "⚠️ 응답을 받는 중 오류가 발생했습니다."
    }

    fun sendChatMessage(query: String, sessionId: String): Flow<String> = flow {
        val userMessage = ChatEntity(
            sessionId = sessionId,
            message = query,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessageAndEnsureSession(userMessage, query)

        try {
            val response = apiService.sendChatStream(ChatRequest(query, sessionId))
            var fullResponse = ""
            processStreamingResponse(response) { chunk ->
                fullResponse += chunk
                emit(chunk)
            }

            val aiMessage = ChatEntity(
                sessionId = sessionId,
                message = fullResponse,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(aiMessage)

        } catch (e: Exception) {
            Log.e(TAG, "Stream error", e)
            emit(ERROR_MESSAGE)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getSessionsWithLastMessage(): List<ChatSessionWithPreview> = chatDao.getSessionsWithLastMessage()

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun deleteMessage(messageId: Long) {
        chatDao.deleteMessageById(messageId)
    }

    suspend fun getMessagesForSession(sessionId: String) = chatDao.getMessagesForSession(sessionId)

    private suspend fun processStreamingResponse(
        response: okhttp3.ResponseBody,
        onChunkReceived: suspend (String) -> Unit
    ) {
        response.source().use { source ->
            while (true) {
                val line = source.readUtf8Line() ?: break
                if (line.startsWith("data:")) {
                    val jsonString = line.substringAfter("data:").trim()
                    if (jsonString.isNotEmpty()) {
                        try {
                            val json = JSONObject(jsonString)
                            val chunk = json.getString(CHUNK_KEY)
                            Log.d(TAG, "✅ Parsed: $chunk")
                            onChunkReceived(chunk)
                        } catch (e: JSONException) {
                            Log.w(TAG, "Invalid JSON: $jsonString", e)
                        }
                    }
                }
            }
        }
    }

    suspend fun checkHealth(): Result<String> {
        return try {
            val response = apiService.healthCheck()
            Result.success(response.string())
        } catch (e: Exception) {
            Log.e(TAG, "Health check failed", e)
            Result.failure(e)
        }
    }
}
