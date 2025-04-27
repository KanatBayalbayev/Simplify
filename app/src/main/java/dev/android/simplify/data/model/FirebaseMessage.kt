package dev.android.simplify.data.model

import java.util.Date

data class FirebaseMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Date = Date(),
    val readBy: Map<String, Boolean> = mapOf()
)
