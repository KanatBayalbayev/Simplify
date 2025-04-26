package dev.android.simplify.domain.usecase

import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.repository.AuthRepository

class ForgotPasswordUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String): AuthResult<Unit> {
        return authRepository.sendPasswordResetEmail(email)
    }
}