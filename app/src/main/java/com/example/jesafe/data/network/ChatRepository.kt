package com.example.jesafe.data.network

import android.util.Log
import com.example.jesafe.data.local.ChatDao
import com.example.jesafe.data.local.ChatEntity
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
        // 1. 사용자 메시지를 트랜잭션으로 저장 (세션이 없으면 여기서 생성)
        val userMessage = ChatEntity(
            sessionId = sessionId,
            message = query,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessageAndEnsureSession(userMessage, query)

        try {
            // 2. 서버에 메시지 전송 및 스트리밍 응답 수신
            val response = apiService.sendChatStream(ChatRequest(query, sessionId))
            var fullResponse = ""
            processStreamingResponse(response) { chunk ->
                fullResponse += chunk
                emit(chunk)
            }

            // 3. AI 응답을 트랜잭션으로 저장 (세션이 확실히 존재)
            val aiMessage = ChatEntity(
                sessionId = sessionId,
                message = fullResponse,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            // AI 메시지 저장 시에는 제목을 생성할 필요가 없으므로, 기존 세션 제목을 그대로 사용합니다.
            chatDao.insertMessage(aiMessage)

        } catch (e: Exception) {
            Log.e(TAG, "Stream error", e)
            emit(ERROR_MESSAGE)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getAllSessions() = chatDao.getAllSessions()

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
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
