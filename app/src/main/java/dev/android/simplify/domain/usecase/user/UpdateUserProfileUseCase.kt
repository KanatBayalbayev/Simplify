package dev.android.simplify.domain.usecase.user

import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.repository.UserRepository

class UpdateUserProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User): AuthResult<Unit> {
        return userRepository.updateUserProfile(user)
    }
}