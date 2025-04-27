package dev.android.simplify.data.model

import java.util.Date

data class FirebaseChat(
    val id: String = "",
    val participants: Map<String, Boolean> = mapOf(),
    val lastMessageId: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
