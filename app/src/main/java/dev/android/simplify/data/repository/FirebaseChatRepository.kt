package dev.android.simplify.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.simplify.data.mapper.toDomainChat
import dev.android.simplify.data.mapper.toDomainUser
import dev.android.simplify.data.mapper.toDomainUserStatus
import dev.android.simplify.data.model.FirebaseChat
import dev.android.simplify.data.model.FirebaseMessage
import dev.android.simplify.data.model.FirebaseUserData
import dev.android.simplify.data.model.FirebaseUserStatus
import dev.android.simplify.domain.model.Chat
import dev.android.simplify.domain.model.ChatWithUser
import dev.android.simplify.domain.model.Message
import dev.android.simplify.domain.model.UserStatus
import dev.android.simplify.domain.repository.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class FirebaseChatRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : ChatRepository {

    private val chatsRef = firebaseDatabase.getReference("chats")
    private val messagesRef = firebaseDatabase.getReference("messages")
    private val usersRef = firebaseDatabase.getReference("users")
    private val userStatusRef = firebaseDatabase.getReference("userStatus")

    override fun getUserChats(): Flow<List<ChatWithUser>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid ?: run {
            trySend(emptyList())
            return@callbackFlow
        }

        val chatsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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

                // Получаем дополнительную информацию для каждого чата
                val chatWithUserList = chatList.mapNotNull { chat ->
                    // Определяем ID собеседника (другой участник, не текущий пользователь)
                    val otherUserId = chat.participants.keys.find { it != currentUserId } ?: return@mapNotNull null

                    // Получаем информацию о собеседнике
                    val otherUserSnapshot = usersRef.child(otherUserId).get()
                    val otherUser = otherUserSnapshot.await().getValue(FirebaseUserData::class.java)?.toDomainUser()
                        ?: return@mapNotNull null

                    // Получаем информацию о последнем сообщении
                    val lastMessageSnapshot = messagesRef.child(chat.lastMessageId).get()
                    val lastMessage = lastMessageSnapshot.await().getValue(FirebaseMessage::class.java)

                    // Получаем статус пользователя
                    val userStatusSnapshot = userStatusRef.child(otherUserId).get()
                    val userStatus = userStatusSnapshot.await().getValue(FirebaseUserStatus::class.java)?.toDomainUserStatus()

                    // Считаем количество непрочитанных сообщений
                    val unreadMessagesSnapshot = messagesRef.orderByChild("chatId").equalTo(chat.id)
                        .get()
                    val unreadMessages = unreadMessagesSnapshot.await().children.count { messageSnapshot ->
                        val message = messageSnapshot.getValue(FirebaseMessage::class.java)
                        message?.senderId != currentUserId && message?.readBy?.get(currentUserId) != true
                    }

                    // Создаем объект ChatWithUser
                    ChatWithUser(
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
                }

                trySend(chatWithUserList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Ошибка при получении данных
            }
        }

        // Слушаем изменения в чатах, где участвует текущий пользователь
        chatsRef.orderByChild("participants/$currentUserId").equalTo(true)
            .addValueEventListener(chatsListener)

        awaitClose {
            chatsRef.removeEventListener(chatsListener)
        }
    }

    override suspend fun getChatById(chatId: String): Chat? {
        return try {
            val chatSnapshot = chatsRef.child(chatId).get().await()
            val firebaseChat = chatSnapshot.getValue(FirebaseChat::class.java) ?: return null

            // Получаем последнее сообщение, если оно есть
            val lastMessage = if (firebaseChat.lastMessageId.isNotEmpty()) {
                val lastMessageSnapshot = messagesRef.child(firebaseChat.lastMessageId).get().await()
                val firebaseMessage = lastMessageSnapshot.getValue(FirebaseMessage::class.java)

                firebaseMessage?.let {
                    val currentUserId = firebaseAuth.currentUser?.uid ?: ""
                    toDomainMessage(it, currentUserId)
                }
            } else null

            firebaseChat.toDomainChat(lastMessage)
        } catch (e: Exception) {
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

        val messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messageList = mutableListOf<Message>()

                for (messageSnapshot in snapshot.children) {
                    val firebaseMessage = messageSnapshot.getValue(FirebaseMessage::class.java)
                    firebaseMessage?.let {
                        messageList.add(toDomainMessage(it, currentUserId))
                    }
                }

                // Сортируем сообщения по времени
                messageList.sortBy { it.timestamp }

                trySend(messageList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Ошибка при получении данных
            }
        }

        // Слушаем изменения в сообщениях данного чата
        messagesRef.orderByChild("chatId").equalTo(chatId)
            .addValueEventListener(messagesListener)

        awaitClose {
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
            // Получаем все непрочитанные сообщения в этом чате
            val messagesSnapshot = messagesRef.orderByChild("chatId").equalTo(chatId).get().await()

            // Помечаем каждое непрочитанное сообщение как прочитанное для данного пользователя
            for (messageSnapshot in messagesSnapshot.children) {
                val firebaseMessage = messageSnapshot.getValue(FirebaseMessage::class.java)
                if (firebaseMessage != null && firebaseMessage.readBy[userId] != true) {
                    messagesRef.child(firebaseMessage.id).child("readBy").child(userId).setValue(true).await()
                }
            }
        } catch (e: Exception) {
            // Обработка ошибок
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
        val messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.children.count { messageSnapshot ->
                    val message = messageSnapshot.getValue(FirebaseMessage::class.java)
                    message?.senderId != userId && message?.readBy?.get(userId) != true
                }

                trySend(count)
            }

            override fun onCancelled(error: DatabaseError) {
                // Ошибка при получении данных
            }
        }

        messagesRef.orderByChild("chatId").equalTo(chatId)
            .addValueEventListener(messagesListener)

        awaitClose {
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