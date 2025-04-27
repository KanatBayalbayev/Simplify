package dev.android.simplify.domain.usecase.chat

import dev.android.simplify.domain.repository.ChatRepository

class CreateChatUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(currentUserId: String, otherUserId: String): String {
        // Проверим, существует ли уже чат между этими пользователями
        val existingChatId = chatRepository.getChatBetweenUsers(currentUserId, otherUserId)

        // Если чат уже существует, возвращаем его ID
        if (existingChatId != null) {
            return existingChatId
        }

        // Иначе создаем новый чат
        return chatRepository.createChat(currentUserId, otherUserId)
    }
}