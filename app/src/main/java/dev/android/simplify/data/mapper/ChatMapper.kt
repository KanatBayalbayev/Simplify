package dev.android.simplify.data.mapper

import dev.android.simplify.data.model.FirebaseChat
import dev.android.simplify.data.model.FirebaseMessage
import dev.android.simplify.data.model.FirebaseUserData
import dev.android.simplify.data.source.local.entity.ChatEntity
import dev.android.simplify.data.source.local.entity.ChatWithUserEntity
import dev.android.simplify.data.source.local.entity.MessageEntity
import dev.android.simplify.data.source.local.entity.UserEntity
import dev.android.simplify.data.source.local.relation.ChatWithUserRelation
import dev.android.simplify.domain.model.Chat
import dev.android.simplify.domain.model.ChatWithUser
import dev.android.simplify.domain.model.Message
import dev.android.simplify.domain.model.User

// Маппер из Firebase модели в Entity модель
fun FirebaseChat.toEntity(): ChatEntity {
    return ChatEntity(
        id = id,
        participants = participants.keys.joinToString(","),
        lastMessageId = lastMessageId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Маппер из Entity модели в Domain модель
fun ChatEntity.toDomainChat(lastMessage: Message? = null): Chat {
    return Chat(
        id = id,
        participants = participants.split(",").filter { it.isNotEmpty() },
        lastMessage = lastMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Маппер из Firebase пользователя в Entity модель
fun FirebaseUserData.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        lastSeen = lastSeen,
        isOnline = isOnline,
        lastUpdated = lastUpdated
    )
}

// Маппер из Entity пользователя в Domain модель
fun UserEntity.toDomainUser(): User {
    return User(
        id = id,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl
    )
}

// Маппер из Firebase сообщения в Entity модель
fun FirebaseMessage.toEntity(currentUserId: String): MessageEntity {
    return MessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        isRead = readBy[currentUserId] == true
    )
}

// Маппер из Entity сообщения в Domain модель
fun MessageEntity.toDomainMessage(): Message {
    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        text = text,
        timestamp = timestamp,
        isRead = isRead
    )
}

// Маппер из Domain модели ChatWithUser в Entity модель
fun ChatWithUser.toEntity(): Pair<ChatWithUserEntity, UserEntity> {
    val chatWithUserEntity = ChatWithUserEntity(
        chatId = chat.id,
        userId = user.id,
        lastMessageText = lastMessageText,
        lastMessageSentByMe = lastMessageSentByMe,
        lastMessageTime = lastMessageTime,
        isOnline = isOnline,
        unreadCount = unreadCount
    )

    val userEntity = UserEntity(
        id = user.id,
        email = user.email,
        displayName = user.displayName,
        photoUrl = user.photoUrl,
        lastSeen = lastMessageTime, // Используем время последнего сообщения
        isOnline = isOnline,
        lastUpdated = lastMessageTime
    )

    return Pair(chatWithUserEntity, userEntity)
}

// Маппер из связанных Entity в Domain модель ChatWithUser
fun ChatWithUserRelation.toDomainChatWithUser(): ChatWithUser {
    return ChatWithUser(
        chat = chatEntity.toDomainChat(),
        user = userEntity.toDomainUser(),
        lastMessageText = chatWithUserEntity.lastMessageText,
        lastMessageSentByMe = chatWithUserEntity.lastMessageSentByMe,
        lastMessageTime = chatWithUserEntity.lastMessageTime,
        isOnline = chatWithUserEntity.isOnline,
        unreadCount = chatWithUserEntity.unreadCount
    )
}