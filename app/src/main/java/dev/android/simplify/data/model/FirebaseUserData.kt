package dev.android.simplify.data.model

import java.util.Date

data class FirebaseUserData(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val lastSeen: Date = Date(),
    val isOnline: Boolean = false,
    val lastUpdated: Date = Date()
)