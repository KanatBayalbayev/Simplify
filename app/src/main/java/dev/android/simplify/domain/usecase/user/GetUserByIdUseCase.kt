package dev.android.simplify.domain.usecase.user

import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.repository.UserRepository

class GetUserByIdUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(userId: String): User? {
        return userRepository.getUserById(userId)
    }
}