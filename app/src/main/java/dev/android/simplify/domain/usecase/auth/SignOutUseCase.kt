package dev.android.simplify.domain.usecase.auth

import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.repository.AuthRepository

class SignOutUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(): AuthResult<Unit> {
        return authRepository.signOut()
    }
}