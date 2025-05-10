package dev.android.simplify.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.android.simplify.data.source.local.entity.ChatWithUserEntity
import dev.android.simplify.data.source.local.relation.ChatWithUserRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatWithUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatWithUser(chatWithUser: ChatWithUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatWithUsers(chatWithUsers: List<ChatWithUserEntity>)

    @Transaction
    @Query("""
        SELECT cu.*, c.*, u.*
        FROM chat_with_user cu
        INNER JOIN chats c ON cu.chatId = c.id
        INNER JOIN users u ON cu.userId = u.id
        ORDER BY cu.lastMessageTime DESC
    """)
    fun getAllChatWithUsers(): Flow<List<ChatWithUserRelation>>

    @Query("DELETE FROM chat_with_user")
    suspend fun deleteAllChatWithUsers()
}