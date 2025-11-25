package com.example.jesafe.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val lastUpdated: Long
)

data class ChatSessionWithPreview(
    @Embedded
    val session: ChatSessionEntity,
    val lastMessage: String?
)
