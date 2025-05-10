package dev.android.simplify.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity для хранения пользователей в базе данных
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String,
    val lastSeen: Date,
    val isOnline: Boolean,
    val lastUpdated: Date
)
