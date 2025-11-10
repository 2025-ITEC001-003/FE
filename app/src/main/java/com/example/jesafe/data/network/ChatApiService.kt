package com.example.jesafe.data.network

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * 채팅 요청 데이터 클래스
 *
 * @property query 사용자의 질문 텍스트
 * @property session_id 세션 고유 식별자
 */
data class ChatRequest(
    val query: String,
    val session_id: String
)

/**
 * AI 채팅 서버와 통신하는 Retrofit API 서비스 인터페이스
 */
interface ChatApiService {
    /**
     * 채팅 메시지를 전송하고 스트리밍 방식으로 응답을 받습니다.
     *
     * @param request 채팅 요청 (질문과 세션 ID 포함)
     * @return 스트리밍 응답 본문
     */
    @POST("chat-stream")
    @Streaming
    suspend fun sendChatStream(@Body request: ChatRequest): ResponseBody

    /**
     * 서버 상태를 확인합니다.
     *
     * @return 서버 상태 응답
     */
    @GET("/")
    suspend fun healthCheck(): ResponseBody
}

