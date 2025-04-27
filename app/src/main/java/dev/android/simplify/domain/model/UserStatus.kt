package dev.android.simplify.domain.model

import java.util.Date

data class UserStatus(
    val userId: String,
    val isOnline: Boolean = false,
    val lastSeen: Date = Date()
)
