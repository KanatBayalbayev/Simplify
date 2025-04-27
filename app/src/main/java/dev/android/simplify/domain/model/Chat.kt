package dev.android.simplify.domain.model

import java.util.Date

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: Message? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)