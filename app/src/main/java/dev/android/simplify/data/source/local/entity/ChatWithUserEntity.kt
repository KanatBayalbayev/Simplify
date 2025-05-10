package dev.android.simplify.data.source.local.entity

import androidx.room.Entity
import java.util.Date

/**
 * Вспомогательный класс для представления связи чата с пользователем
 * для хранения в базе данных
 */
@Entity(
    tableName = "chat_with_user",
    primaryKeys = ["chatId", "userId"]
)
data class ChatWithUserEntity(
    val chatId: String,
    val userId: String,
    val lastMessageText: String,
    val lastMessageSentByMe: Boolean,
    val lastMessageTime: Date,
    val isOnline: Boolean,
    val unreadCount: Int
)
