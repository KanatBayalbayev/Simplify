package dev.android.simplify.domain.usecase.auth

import dev.android.simplify.domain.repository.AuthRepository

class ClearCredentialsUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() {
        authRepository.clearCredentials()
    }
}