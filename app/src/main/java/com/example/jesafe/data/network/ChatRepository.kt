package com.example.jesafe.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 채팅 API와의 통신을 담당하는 저장소
 *
 * 스트리밍 기반의 JSON 응답을 실시간으로 파싱하고 전달합니다.
 * 불완전한 JSON 객체를 버퍼링하여 완전한 JSON만 처리합니다.
 *
 * @property apiService 채팅 API 서비스 인터페이스
 */
@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ChatApiService
) {
    companion object {
        private const val TAG = "ChatRepository"
        private const val BUFFER_SIZE = 1024
        private const val CHUNK_KEY = "chunk"
        private const val ERROR_MESSAGE = "⚠️ 응답을 받는 중 오류가 발생했습니다."
    }

    /**
     * 채팅 메시지를 서버로 전송하고 스트리밍 응답을 받습니다.
     *
     * 서버로부터 실시간으로 전송되는 JSON 청크를 파싱하여
     * Flow를 통해 텍스트를 순차적으로 전달합니다.
     *
     * @param query 사용자의 질문
     * @param sessionId 대화 세션 ID
     * @return 실시간으로 전달되는 응답 텍스트 Flow
     */
    fun sendChatMessage(query: String, sessionId: String): Flow<String> = flow {
        try {
            val response = apiService.sendChatStream(ChatRequest(query, sessionId))
            processStreamingResponse(response) { chunk ->
                emit(chunk)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Stream error", e)
            emit(ERROR_MESSAGE)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 스트리밍 응답을 처리하여 완전한 JSON 객체를 추출합니다.
     *
     * 버퍼를 사용하여 불완전한 JSON을 보관하고,
     * 완전한 JSON 객체가 수신되면 파싱하여 전달합니다.
     *
     * @param response 스트리밍 응답 본문
     * @param onChunkReceived 청크 수신 시 호출되는 콜백
     */
    private suspend fun processStreamingResponse(
        response: okhttp3.ResponseBody,
        onChunkReceived: suspend (String) -> Unit
    ) {
        response.byteStream().bufferedReader().use { reader ->
            val buffer = JsonBuffer()
            val charBuffer = CharArray(BUFFER_SIZE)

            while (true) {
                val charsRead = reader.read(charBuffer)
                if (charsRead == -1) break

                buffer.append(charBuffer, charsRead)
                buffer.processCompleteJsonObjects(onChunkReceived)
            }

            buffer.processRemainingData(onChunkReceived)
        }
    }

    /**
     * JSON 버퍼를 관리하는 내부 클래스
     *
     * 스트리밍으로 수신되는 불완전한 JSON 데이터를 버퍼링하고,
     * 완전한 JSON 객체만 추출하여 처리합니다.
     */
    private inner class JsonBuffer {
        private val buffer = StringBuilder()

        fun append(chars: CharArray, length: Int) {
            buffer.append(chars, 0, length)
        }

        suspend fun processCompleteJsonObjects(onChunkParsed: suspend (String) -> Unit) {
            val processedLength = extractAndParseJsonObjects(buffer.toString(), onChunkParsed)
            if (processedLength > 0) {
                buffer.delete(0, processedLength)
            }
        }

        suspend fun processRemainingData(onChunkParsed: suspend (String) -> Unit) {
            if (buffer.isNotEmpty()) {
                extractAndParseJsonObjects(buffer.toString(), onChunkParsed)
                buffer.clear()
            }
        }

        /**
         * 문자열에서 완전한 JSON 객체들을 추출하고 파싱합니다.
         *
         * 여러 개의 JSON 객체가 연속으로 있을 수 있으므로
         * 모든 완전한 JSON을 찾아 처리합니다.
         *
         * @param data 파싱할 문자열
         * @param onChunkParsed 청크 파싱 완료 시 호출되는 콜백
         * @return 처리된 문자열의 마지막 인덱스
         */
        private suspend fun extractAndParseJsonObjects(
            data: String,
            onChunkParsed: suspend (String) -> Unit
        ): Int {
            var currentIndex = 0
            var lastProcessedIndex = 0

            while (currentIndex < data.length) {
                val jsonRange = findCompleteJsonObject(data, currentIndex) ?: break

                val jsonString = data.substring(jsonRange.first, jsonRange.second + 1)
                parseAndEmitChunk(jsonString, onChunkParsed)

                lastProcessedIndex = jsonRange.second + 1
                currentIndex = lastProcessedIndex
            }

            return lastProcessedIndex
        }

        /**
         * 완전한 JSON 객체의 범위를 찾습니다.
         *
         * 중괄호 쌍을 찾아 JSON 객체의 시작과 끝 위치를 반환합니다.
         *
         * @param data 검색할 문자열
         * @param startFrom 검색 시작 위치
         * @return JSON 객체의 (시작 인덱스, 끝 인덱스) 쌍, 찾지 못하면 null
         */
        private fun findCompleteJsonObject(data: String, startFrom: Int): Pair<Int, Int>? {
            val openBrace = data.indexOf('{', startFrom)
            if (openBrace == -1) return null

            val closeBrace = data.indexOf('}', openBrace)
            if (closeBrace == -1) return null

            return Pair(openBrace, closeBrace)
        }

        /**
         * JSON 문자열을 파싱하고 chunk 값을 추출하여 전달합니다.
         *
         * @param jsonString 파싱할 JSON 문자열
         * @param onChunkParsed 청크 추출 시 호출되는 콜백
         */
        private suspend fun parseAndEmitChunk(
            jsonString: String,
            onChunkParsed: suspend (String) -> Unit
        ) {
            try {
                extractChunkFromJson(jsonString)?.let { chunk ->
                    Log.d(TAG, "✅ Parsed: $chunk")
                    onChunkParsed(chunk)
                }
            } catch (e: JSONException) {
                Log.w(TAG, "Invalid JSON: $jsonString", e)
            }
        }

        /**
         * JSON 객체에서 chunk 필드 값을 추출합니다.
         *
         * @param jsonString 파싱할 JSON 문자열
         * @return chunk 필드 값, 없거나 오류 시 null
         */
        private fun extractChunkFromJson(jsonString: String): String? {
            return try {
                val chunkValue = JSONObject(jsonString).optString(CHUNK_KEY, "")
                chunkValue.takeIf { it.isNotEmpty() }
            } catch (_: JSONException) {
                null
            }
        }
    }

    /**
     * 서버 헬스 체크를 수행합니다.
     *
     * @return 성공 시 응답 문자열을 담은 Result, 실패 시 예외를 담은 Result
     */
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
