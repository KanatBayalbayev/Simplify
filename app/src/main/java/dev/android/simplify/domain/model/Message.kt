package dev.android.simplify.domain.model

import java.util.Date

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Date = Date(),
    val isRead: Boolean = false
)
