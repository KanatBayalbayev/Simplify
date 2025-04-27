package dev.android.simplify.domain.usecase.user

import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserProfileUseCase(private val userRepository: UserRepository) {
    operator fun invoke(): Flow<User?> {
        return userRepository.getCurrentUserProfile()
    }
}