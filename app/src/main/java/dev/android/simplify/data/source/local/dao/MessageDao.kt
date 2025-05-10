package dev.android.simplify.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.android.simplify.data.source.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesByChatId(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("UPDATE messages SET isRead = 1 WHERE chatId = :chatId AND senderId != :userId")
    suspend fun markMessagesAsRead(chatId: String, userId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId AND senderId != :userId AND isRead = 0")
    fun getUnreadMessagesCount(chatId: String, userId: String): Flow<Int>

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}