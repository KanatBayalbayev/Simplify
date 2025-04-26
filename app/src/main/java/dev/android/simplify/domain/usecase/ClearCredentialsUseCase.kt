package dev.android.simplify.domain.usecase

import dev.android.simplify.domain.repository.AuthRepository

class ClearCredentialsUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() {
        authRepository.clearCredentials()
    }
}