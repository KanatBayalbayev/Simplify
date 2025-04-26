package dev.android.simplify.domain.usecase

import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {

    operator fun invoke(): Flow<AuthResult<User?>> {
        return authRepository.getCurrentUser()
    }
}