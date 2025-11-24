package com.example.jesafe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY lastUpdated DESC")
    suspend fun getAllSessions(): List<ChatSessionEntity>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: String): List<ChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Insert
    suspend fun insertMessage(message: ChatEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Transaction
    suspend fun insertMessageAndEnsureSession(message: ChatEntity, title: String) {
        // 세션이 없으면 새로 생성
        if (getSessionById(message.sessionId) == null) {
            insertSession(ChatSessionEntity(id = message.sessionId, title = title, lastUpdated = message.timestamp))
        }
        // 메시지 저장
        insertMessage(message)
    }
}
