package dev.android.simplify.domain.usecase.chat

import dev.android.simplify.domain.model.Message
import dev.android.simplify.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetChatMessagesUseCase(private val chatRepository: ChatRepository) {
    operator fun invoke(chatId: String): Flow<List<Message>> {
        return chatRepository.getChatMessages(chatId)
    }
}