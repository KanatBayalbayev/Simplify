package dev.android.simplify.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity для хранения сообщений в базе данных
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val senderId: String,
    val text: String,
    val timestamp: Date,
    val isRead: Boolean
)
