package dev.android.simplify.data.mapper

import com.google.firebase.auth.FirebaseUser
import dev.android.simplify.data.model.FirebaseChat
import dev.android.simplify.data.model.FirebaseMessage
import dev.android.simplify.data.model.FirebaseUserData
import dev.android.simplify.data.model.FirebaseUserStatus
import dev.android.simplify.domain.model.Chat
import dev.android.simplify.domain.model.Message
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.model.UserStatus

fun FirebaseUser.toDomainUser(): User {
    return User(
        id = uid,
        email = email.orEmpty(),
        displayName = displayName.orEmpty(),
        photoUrl = photoUrl?.toString().orEmpty()
    )
}

fun FirebaseUserData.toDomainUser(): User {
    return User(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl
    )
}

// Маппер из доменного пользователя в Firebase модель
fun User.toFirebaseUser(): FirebaseUserData {
    return FirebaseUserData(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl
    )
}

// Маппер из Firebase чата в доменную модель
fun FirebaseChat.toDomainChat(lastMessage: Message? = null): Chat {
    return Chat(
        id = id,
        participants = participants.keys.toList(),
        lastMessage = lastMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Маппер из Firebase сообщения в доменную модель
fun FirebaseMessage.toDomainMessage(currentUserId: String): Message {
    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        isRead = readBy[currentUserId] == true
    )
}

// Маппер из Firebase статуса пользователя в доменную модель
fun FirebaseUserStatus.toDomainUserStatus(): UserStatus {
    return UserStatus(
        userId = userId,
        isOnline = isOnline,
        lastSeen = lastSeen
    )
}