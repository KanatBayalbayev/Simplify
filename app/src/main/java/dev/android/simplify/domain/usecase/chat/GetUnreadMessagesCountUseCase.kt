package dev.android.simplify.domain.usecase.chat

import dev.android.simplify.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetUnreadMessagesCountUseCase(private val chatRepository: ChatRepository) {
    operator fun invoke(chatId: String, userId: String): Flow<Int> {
        return chatRepository.getUnreadMessagesCount(chatId, userId)
    }
}