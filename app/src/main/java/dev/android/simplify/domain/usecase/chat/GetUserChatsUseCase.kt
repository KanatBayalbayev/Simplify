package dev.android.simplify.domain.usecase.chat

import dev.android.simplify.domain.model.ChatWithUser
import dev.android.simplify.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetUserChatsUseCase(private val chatRepository: ChatRepository) {
    operator fun invoke(): Flow<List<ChatWithUser>> {
        return chatRepository.getUserChats()
    }
}