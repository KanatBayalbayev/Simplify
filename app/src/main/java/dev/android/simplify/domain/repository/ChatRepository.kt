package dev.android.simplify.domain.repository

import dev.android.simplify.domain.model.Chat
import dev.android.simplify.domain.model.ChatWithUser
import dev.android.simplify.domain.model.Message
import dev.android.simplify.domain.model.UserStatus
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /**
     * Получение списка чатов текущего пользователя
     */
    fun getUserChats(): Flow<List<ChatWithUser>>

    /**
     * Получение конкретного чата по ID
     */
    suspend fun getChatById(chatId: String): Chat?

    /**
     * Создание нового чата между пользователями
     */
    suspend fun createChat(currentUserId: String, otherUserId: String): String

    /**
     * Получение истории сообщений для конкретного чата
     */
    fun getChatMessages(chatId: String): Flow<List<Message>>

    /**
     * Отправка сообщения
     */
    suspend fun sendMessage(chatId: String, senderId: String, text: String): Message

    /**
     * Отметить сообщения как прочитанные
     */
    suspend fun markMessagesAsRead(chatId: String, userId: String)

    /**
     * Проверка существования чата между пользователями
     */
    suspend fun getChatBetweenUsers(currentUserId: String, otherUserId: String): String?

    /**
     * Получение статуса пользователя (онлайн/офлайн)
     */
    fun getUserStatus(userId: String): Flow<UserStatus>

    /**
     * Установка статуса пользователя
     */
    suspend fun setUserStatus(userId: String, isOnline: Boolean)

    /**
     * Получение количества непрочитанных сообщений для чата
     */
    fun getUnreadMessagesCount(chatId: String, userId: String): Flow<Int>
}