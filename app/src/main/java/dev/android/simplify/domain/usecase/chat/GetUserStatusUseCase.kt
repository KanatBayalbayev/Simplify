package dev.android.simplify.domain.usecase.chat

import dev.android.simplify.domain.repository.ChatRepository

class GetUserStatusUseCase(private val chatRepository: ChatRepository) {
    operator fun invoke(userId: String) = chatRepository.getUserStatus(userId)
}