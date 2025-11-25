package com.example.jesafe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ChatDao {
    @Transaction
    @Query("""
        SELECT cs.*, (
            SELECT message FROM chat_messages
            WHERE sessionId = cs.id
            ORDER BY timestamp DESC
            LIMIT 1
        ) as lastMessage
        FROM chat_sessions cs
        ORDER BY cs.lastUpdated DESC
    """)
    suspend fun getSessionsWithLastMessage(): List<ChatSessionWithPreview>

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

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    @Transaction
    suspend fun insertMessageAndEnsureSession(message: ChatEntity, title: String) {
        if (getSessionById(message.sessionId) == null) {
            insertSession(ChatSessionEntity(id = message.sessionId, title = title, lastUpdated = message.timestamp))
        }
        insertMessage(message)
    }
}
