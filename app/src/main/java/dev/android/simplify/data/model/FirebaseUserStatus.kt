package dev.android.simplify.data.model

import java.util.Date

data class FirebaseUserStatus(
    val userId: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Date = Date()
)
