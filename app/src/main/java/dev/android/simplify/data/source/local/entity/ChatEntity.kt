package dev.android.simplify.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity для хранения чатов в базе данных
 */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val participants: String, // Сохраняем как разделенный список
    val lastMessageId: String,
    val createdAt: Date,
    val updatedAt: Date
)
