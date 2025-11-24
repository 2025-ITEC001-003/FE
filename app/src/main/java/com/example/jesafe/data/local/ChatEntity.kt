package com.example.jesafe.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sessionId: String,
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
