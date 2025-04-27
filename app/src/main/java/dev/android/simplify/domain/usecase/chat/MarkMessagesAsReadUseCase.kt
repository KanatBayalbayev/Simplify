package dev.android.simplify.domain.usecase.chat

import dev.android.simplify.domain.repository.ChatRepository

class MarkMessagesAsReadUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(chatId: String, userId: String) {
        chatRepository.markMessagesAsRead(chatId, userId)
    }
}