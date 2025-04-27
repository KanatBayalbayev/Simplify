package dev.android.simplify.domain.usecase.auth

import dev.android.simplify.domain.repository.AuthRepository

class SaveCredentialsUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(email: String, password: String) {
        authRepository.saveCredentials(email, password)
    }
}