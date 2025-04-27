package dev.android.simplify.domain.usecase.chat

import dev.android.simplify.domain.repository.ChatRepository

class SetUserStatusUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(userId: String, isOnline: Boolean) {
        chatRepository.setUserStatus(userId, isOnline)
    }
}