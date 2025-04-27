package dev.android.simplify.domain.usecase.chat

import dev.android.simplify.domain.model.Message
import dev.android.simplify.domain.repository.ChatRepository

class SendMessageUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(chatId: String, senderId: String, text: String): Message {
        return chatRepository.sendMessage(chatId, senderId, text)
    }
}