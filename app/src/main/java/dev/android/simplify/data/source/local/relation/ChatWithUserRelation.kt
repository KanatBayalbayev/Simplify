package dev.android.simplify.data.source.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import dev.android.simplify.data.source.local.entity.ChatEntity
import dev.android.simplify.data.source.local.entity.ChatWithUserEntity
import dev.android.simplify.data.source.local.entity.UserEntity

/**
 * Класс для представления связи между ChatWithUserEntity, ChatEntity и UserEntity
 * с использованием аннотаций Room для поддержки отношений
 */
data class ChatWithUserRelation(
    @Embedded
    val chatWithUserEntity: ChatWithUserEntity,

    @Relation(
        parentColumn = "chatId",
        entityColumn = "id"
    )
    val chatEntity: ChatEntity,

    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val userEntity: UserEntity
)
