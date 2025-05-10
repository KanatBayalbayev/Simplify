package dev.android.simplify.data.source.local

import dev.android.simplify.data.mapper.toDomainChatWithUser
import dev.android.simplify.data.mapper.toDomainMessage
import dev.android.simplify.data.source.local.dao.ChatDao
import dev.android.simplify.data.source.local.dao.ChatWithUserDao
import dev.android.simplify.data.source.local.dao.MessageDao
import dev.android.simplify.data.source.local.dao.UserDao
import dev.android.simplify.data.source.local.entity.ChatEntity
import dev.android.simplify.data.source.local.entity.ChatWithUserEntity
import dev.android.simplify.data.source.local.entity.MessageEntity
import dev.android.simplify.data.source.local.entity.UserEntity
import dev.android.simplify.domain.model.ChatWithUser
import dev.android.simplify.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Источник данных для работы с локальной базой данных чатов
 */
class LocalChatDataSource(
    private val chatDao: ChatDao,
    private val userDao: UserDao,
    private val messageDao: MessageDao,
    private val chatWithUserDao: ChatWithUserDao
) {
    // Методы для работы с чатами
    suspend fun insertChat(chat: ChatEntity) {
        chatDao.insertChat(chat)
    }

    suspend fun insertChats(chats: List<ChatEntity>) {
        chatDao.insertChats(chats)
    }

    suspend fun getChatById(chatId: String): ChatEntity? {
        return chatDao.getChatById(chatId)
    }

    fun getAllChats(): Flow<List<ChatEntity>> {
        return chatDao.getAllChats()
    }

    suspend fun deleteAllChats() {
        chatDao.deleteAllChats()
    }

    // Методы для работы с пользователями
    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun insertUsers(users: List<UserEntity>) {
        userDao.insertUsers(users)
    }

    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun searchUsersByEmail(query: String): List<UserEntity> {
        return userDao.searchUsersByEmail(query)
    }

    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }

    // Методы для работы с сообщениями
    suspend fun insertMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }

    suspend fun insertMessages(messages: List<MessageEntity>) {
        messageDao.insertMessages(messages)
    }

    fun getMessagesByChatId(chatId: String): Flow<List<Message>> {
        return messageDao.getMessagesByChatId(chatId)
            .map { messages -> messages.map { it.toDomainMessage() } }
    }

    suspend fun getMessageById(messageId: String): MessageEntity? {
        return messageDao.getMessageById(messageId)
    }

    suspend fun markMessagesAsRead(chatId: String, userId: String) {
        messageDao.markMessagesAsRead(chatId, userId)
    }

    fun getUnreadMessagesCount(chatId: String, userId: String): Flow<Int> {
        return messageDao.getUnreadMessagesCount(chatId, userId)
    }

    suspend fun deleteMessagesByChatId(chatId: String) {
        messageDao.deleteMessagesByChatId(chatId)
    }

    suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    // Методы для работы с ChatWithUser
    suspend fun insertChatWithUser(chatWithUser: ChatWithUserEntity) {
        chatWithUserDao.insertChatWithUser(chatWithUser)
    }

    suspend fun insertChatWithUsers(chatWithUsers: List<ChatWithUserEntity>) {
        chatWithUserDao.insertChatWithUsers(chatWithUsers)
    }

    fun getAllChatWithUsers(): Flow<List<ChatWithUser>> {
        return chatWithUserDao.getAllChatWithUsers()
            .map { relations -> relations.map { it.toDomainChatWithUser() } }
    }

    suspend fun deleteAllChatWithUsers() {
        chatWithUserDao.deleteAllChatWithUsers()
    }

    // Вспомогательный метод для очистки всей базы данных
    suspend fun clearDatabase() {
        deleteAllChatWithUsers()
        deleteAllMessages()
        deleteAllChats()
        deleteAllUsers()
    }
}