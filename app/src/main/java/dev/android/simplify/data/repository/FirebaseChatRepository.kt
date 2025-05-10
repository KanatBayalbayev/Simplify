package dev.android.simplify.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.simplify.data.mapper.toDomainChat
import dev.android.simplify.data.mapper.toDomainMessage
import dev.android.simplify.data.mapper.toDomainUser
import dev.android.simplify.data.mapper.toDomainUserStatus
import dev.android.simplify.data.mapper.toEntity
import dev.android.simplify.data.model.FirebaseChat
import dev.android.simplify.data.model.FirebaseMessage
import dev.android.simplify.data.model.FirebaseUserData
import dev.android.simplify.data.model.FirebaseUserStatus
import dev.android.simplify.data.source.local.LocalChatDataSource
import dev.android.simplify.domain.model.Chat
import dev.android.simplify.domain.model.ChatWithUser
import dev.android.simplify.domain.model.Message
import dev.android.simplify.domain.model.UserStatus
import dev.android.simplify.domain.repository.ChatRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.emitAll
import java.util.Date
import java.util.UUID

class FirebaseChatRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val localChatDataSource: LocalChatDataSource
) : ChatRepository {

    private val chatsRef = firebaseDatabase.getReference("chats")
    private val messagesRef = firebaseDatabase.getReference("messages")
    private val usersRef = firebaseDatabase.getReference("users")
    private val userStatusRef = firebaseDatabase.getReference("userStatus")

    private val TAG = "FirebaseChatRepo"

    @OptIn(DelicateCoroutinesApi::class)
    override fun getUserChats(): Flow<List<ChatWithUser>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        // Подписываемся на изменения в кэше
        val cacheJob = launch {
            localChatDataSource.getAllChatWithUsers().collect { cachedChats ->
                trySend(cachedChats)
            }
        }

        val chatsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val chatList = mutableListOf<FirebaseChat>()

                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(FirebaseChat::class.java)
                        chat?.let {
                            // Проверяем, что текущий пользователь является участником чата
                            if (it.participants.containsKey(currentUserId)) {
                                chatList.add(it)
                            }
                        }
                    }

                    // Обработка и сохранение данных в локальной базе
                    launch(Dispatchers.IO) {
                        processChatListAndCache(chatList, currentUserId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при обработке данных чатов", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Ошибка получения чатов: ${error.message}", error.toException())
            }
        }

        // Слушаем изменения в чатах пользователя
        val query = chatsRef.orderByChild("participants/$currentUserId").equalTo(true)
        query.addValueEventListener(chatsListener)

        // При закрытии flow удаляем слушатель и отменяем все корутины
        awaitClose {
            cacheJob.cancel()
            query.removeEventListener(chatsListener)
        }
    }

    private suspend fun processChatListAndCache(
        chatList: List<FirebaseChat>,
        currentUserId: String
    ) {
        try {
            // Сохраняем сначала сущности чатов
            val chatEntities = chatList.map { it.toEntity() }
            localChatDataSource.insertChats(chatEntities)

            val chatWithUsers = mutableListOf<ChatWithUser>()

            for (chat in chatList) {
                try {
                    // Находим другого участника чата
                    val otherUserId = chat.participants.keys.find { it != currentUserId }
                        ?: continue

                    // Получаем данные о пользователе
                    val otherUserSnapshot = usersRef.child(otherUserId).get().await()
                    val otherUserData = otherUserSnapshot.getValue(FirebaseUserData::class.java)
                        ?: continue

                    // Сохраняем пользователя в БД
                    localChatDataSource.insertUser(otherUserData.toEntity())

                    val otherUser = otherUserData.toDomainUser()

                    // Получаем последнее сообщение
                    var lastMessage: FirebaseMessage? = null
                    if (chat.lastMessageId.isNotEmpty()) {
                        val lastMessageSnapshot = messagesRef.child(chat.lastMessageId).get().await()
                        lastMessage = lastMessageSnapshot.getValue(FirebaseMessage::class.java)

                        // Сохраняем сообщение в БД, если оно есть
                        lastMessage?.let {
                            localChatDataSource.insertMessage(it.toEntity(currentUserId))
                        }
                    }

                    // Получаем статус пользователя
                    val userStatusSnapshot = userStatusRef.child(otherUserId).get().await()
                    val userStatus = userStatusSnapshot.getValue(FirebaseUserStatus::class.java)

                    // Считаем количество непрочитанных сообщений
                    val unreadMessagesSnapshot = messagesRef.orderByChild("chatId").equalTo(chat.id)
                        .get().await()
                    val unreadMessages = unreadMessagesSnapshot.children.count { messageSnapshot ->
                        val message = messageSnapshot.getValue(FirebaseMessage::class.java)
                        val isUnread = message?.senderId != currentUserId && message?.readBy?.get(currentUserId) != true
                        isUnread
                    }

                    // Создаем объект ChatWithUser
                    val chatWithUser = ChatWithUser(
                        chat = chat.toDomainChat(lastMessage?.let {
                            toDomainMessage(it, currentUserId)
                        }),
                        user = otherUser,
                        lastMessageText = lastMessage?.text ?: "",
                        lastMessageSentByMe = lastMessage?.senderId == currentUserId,
                        lastMessageTime = lastMessage?.timestamp ?: chat.updatedAt,
                        isOnline = userStatus?.isOnline ?: false,
                        unreadCount = unreadMessages
                    )

                    chatWithUsers.add(chatWithUser)

                    // Сохраняем связанные сущности для ChatWithUser
                    val (chatWithUserEntity, _) = chatWithUser.toEntity()
                    localChatDataSource.insertChatWithUser(chatWithUserEntity)

                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка обработки чата ${chat.id}", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при кэшировании чатов", e)
        }
    }


    override suspend fun getChatById(chatId: String): Chat? {
        return try {
            // Сначала пробуем получить из локального кэша
            val cachedChat = localChatDataSource.getChatById(chatId)

            if (cachedChat != null) {
                // Пытаемся получить последнее сообщение из кэша
                val lastMessageId = cachedChat.lastMessageId
                val lastMessage = if (lastMessageId.isNotEmpty()) {
                    localChatDataSource.getMessageById(lastMessageId)?.toDomainMessage()
                } else null

                return cachedChat.toDomainChat(lastMessage)
            }

            // Если в кэше нет, обращаемся к Firebase
            val chatSnapshot = chatsRef.child(chatId).get().await()
            val firebaseChat = chatSnapshot.getValue(FirebaseChat::class.java) ?: return null

            // Получаем последнее сообщение, если оно есть
            val lastMessage = if (firebaseChat.lastMessageId.isNotEmpty()) {
                val lastMessageSnapshot = messagesRef.child(firebaseChat.lastMessageId).get().await()
                val firebaseMessage = lastMessageSnapshot.getValue(FirebaseMessage::class.java)

                firebaseMessage?.let {
                    val currentUserId = firebaseAuth.currentUser?.uid ?: ""
                    // Кэшируем сообщение
                    localChatDataSource.insertMessage(it.toEntity(currentUserId))
                    toDomainMessage(it, currentUserId)
                }
            } else null

            // Кэшируем чат
            localChatDataSource.insertChat(firebaseChat.toEntity())

            firebaseChat.toDomainChat(lastMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении чата по ID", e)
            null
        }
    }

    override suspend fun createChat(currentUserId: String, otherUserId: String): String {
        // Генерируем уникальный ID для чата
        val chatId = UUID.randomUUID().toString()

        // Создаем мапу с участниками чата
        val participants = mapOf(
            currentUserId to true,
            otherUserId to true
        )

        // Создаем новый чат
        val chat = FirebaseChat(
            id = chatId,
            participants = participants,
            createdAt = Date(),
            updatedAt = Date()
        )

        // Сохраняем чат в Firebase
        chatsRef.child(chatId).setValue(chat).await()

        return chatId
    }

    override fun getChatMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid ?: run {
            trySend(emptyList())
            return@callbackFlow
        }

        // Подписываемся на изменения в кэше
        val cacheJob = launch {
            localChatDataSource.getMessagesByChatId(chatId).collect { messages ->
                trySend(messages)
            }
        }

        val messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val messageList = mutableListOf<FirebaseMessage>()

                    for (messageSnapshot in snapshot.children) {
                        val firebaseMessage = messageSnapshot.getValue(FirebaseMessage::class.java)
                        firebaseMessage?.let {
                            messageList.add(it)
                        }
                    }

                    // Сохраняем сообщения в локальную БД
                    launch(Dispatchers.IO) {
                        val messageEntities = messageList.map { it.toEntity(currentUserId) }
                        localChatDataSource.insertMessages(messageEntities)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при обработке сообщений", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Ошибка получения сообщений: ${error.message}")
            }
        }

        // Слушаем изменения в сообщениях данного чата
        messagesRef.orderByChild("chatId").equalTo(chatId)
            .addValueEventListener(messagesListener)

        awaitClose {
            cacheJob.cancel()
            messagesRef.removeEventListener(messagesListener)
        }
    }

    override suspend fun sendMessage(chatId: String, senderId: String, text: String): Message {
        // Генерируем уникальный ID для сообщения
        val messageId = UUID.randomUUID().toString()

        // Создаем новое сообщение
        val message = FirebaseMessage(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            text = text,
            timestamp = Date(),
            readBy = mapOf(senderId to true)
        )

        // Сохраняем сообщение в Firebase
        messagesRef.child(messageId).setValue(message).await()

        // Обновляем информацию о последнем сообщении в чате
        chatsRef.child(chatId).updateChildren(
            mapOf(
                "lastMessageId" to messageId,
                "updatedAt" to Date()
            )
        ).await()

        return Message(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            text = text,
            timestamp = Date(),
            isRead = true
        )
    }

    override suspend fun markMessagesAsRead(chatId: String, userId: String) {
        try {
            Log.d("markMessagesAsRead", "Отмечаем сообщения в чате $chatId как прочитанные для пользователя $userId")
            // Получаем все сообщения в этом чате
            val messagesSnapshot = messagesRef.orderByChild("chatId").equalTo(chatId).get().await()
            var markedCount = 0

            // Помечаем каждое непрочитанное сообщение как прочитанное для данного пользователя
            for (messageSnapshot in messagesSnapshot.children) {
                val firebaseMessage = messageSnapshot.getValue(FirebaseMessage::class.java)
                if (firebaseMessage != null && firebaseMessage.senderId != userId && (firebaseMessage.readBy[userId] != true)) {
                    messagesRef.child(firebaseMessage.id).child("readBy").child(userId).setValue(true).await()
                    markedCount++
                }
            }

            Log.d("markMessagesAsRead", "Отмечено $markedCount сообщений как прочитанные")
        } catch (e: Exception) {
            Log.e("markMessagesAsRead", "Ошибка при отметке сообщений как прочитанных", e)
        }
    }

    override suspend fun getChatBetweenUsers(currentUserId: String, otherUserId: String): String? {
        try {
            // Ищем чат, где оба пользователя являются участниками
            val chatsSnapshot = chatsRef.get().await()

            for (chatSnapshot in chatsSnapshot.children) {
                val chat = chatSnapshot.getValue(FirebaseChat::class.java)
                if (chat != null &&
                    chat.participants.containsKey(currentUserId) &&
                    chat.participants.containsKey(otherUserId)) {
                    return chat.id
                }
            }

            return null
        } catch (e: Exception) {
            return null
        }
    }

    override fun getUserStatus(userId: String): Flow<UserStatus> = callbackFlow {
        val statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(FirebaseUserStatus::class.java)
                if (status != null) {
                    trySend(status.toDomainUserStatus())
                } else {
                    // Если статус не найден, отправляем дефолтный статус (оффлайн)
                    trySend(UserStatus(userId = userId, isOnline = false, lastSeen = Date()))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Ошибка при получении данных
            }
        }

        userStatusRef.child(userId).addValueEventListener(statusListener)

        awaitClose {
            userStatusRef.child(userId).removeEventListener(statusListener)
        }
    }

    override suspend fun setUserStatus(userId: String, isOnline: Boolean) {
        try {
            val status = FirebaseUserStatus(
                userId = userId,
                isOnline = isOnline,
                lastSeen = Date()
            )

            userStatusRef.child(userId).setValue(status).await()
        } catch (e: Exception) {
            // Обработка ошибок
        }
    }

    override fun getUnreadMessagesCount(chatId: String, userId: String): Flow<Int> = callbackFlow {
        // Подписываемся на изменения в кэше
        val cacheJob = launch {
            localChatDataSource.getUnreadMessagesCount(chatId, userId).collect { count ->
                trySend(count)
            }
        }

        val messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.children.count { messageSnapshot ->
                    val message = messageSnapshot.getValue(FirebaseMessage::class.java)
                    message?.senderId != userId && message?.readBy?.get(userId) != true
                }

                trySend(count)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Ошибка при получении непрочитанных сообщений", error.toException())
            }
        }

        messagesRef.orderByChild("chatId").equalTo(chatId)
            .addValueEventListener(messagesListener)

        awaitClose {
            cacheJob.cancel()
            messagesRef.removeEventListener(messagesListener)
        }
    }

    // Вспомогательный метод для преобразования FirebaseMessage в Message
    private fun toDomainMessage(firebaseMessage: FirebaseMessage, currentUserId: String): Message {
        return Message(
            id = firebaseMessage.id,
            chatId = firebaseMessage.chatId,
            senderId = firebaseMessage.senderId,
            text = firebaseMessage.text,
            timestamp = firebaseMessage.timestamp,
            isRead = firebaseMessage.readBy[currentUserId] == true
        )
    }
}