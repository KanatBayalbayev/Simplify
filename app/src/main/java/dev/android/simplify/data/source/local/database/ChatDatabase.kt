package dev.android.simplify.data.source.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.android.simplify.data.source.local.converter.Converters
import dev.android.simplify.data.source.local.dao.ChatDao
import dev.android.simplify.data.source.local.dao.ChatWithUserDao
import dev.android.simplify.data.source.local.dao.MessageDao
import dev.android.simplify.data.source.local.dao.UserDao
import dev.android.simplify.data.source.local.entity.ChatEntity
import dev.android.simplify.data.source.local.entity.ChatWithUserEntity
import dev.android.simplify.data.source.local.entity.MessageEntity
import dev.android.simplify.data.source.local.entity.UserEntity

@Database(
    entities = [
        ChatEntity::class,
        UserEntity::class,
        MessageEntity::class,
        ChatWithUserEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChatDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun chatWithUserDao(): ChatWithUserDao

    companion object {
        private const val DATABASE_NAME = "simplify_chat_database"

        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}