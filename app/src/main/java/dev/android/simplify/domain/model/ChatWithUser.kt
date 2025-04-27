package dev.android.simplify.domain.model

import java.util.Date

data class ChatWithUser(
    val chat: Chat,
    val user: User,
    val lastMessageText: String = "",
    val lastMessageSentByMe: Boolean = false,
    val lastMessageTime: Date = Date(),
    val isOnline: Boolean = false,
    val unreadCount: Int = 0
)
