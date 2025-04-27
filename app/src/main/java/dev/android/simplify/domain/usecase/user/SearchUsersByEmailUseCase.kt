package dev.android.simplify.domain.usecase.user

import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.repository.UserRepository

class SearchUsersByEmailUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(email: String): List<User> {
        return userRepository.searchUsersByEmail(email)
    }
}