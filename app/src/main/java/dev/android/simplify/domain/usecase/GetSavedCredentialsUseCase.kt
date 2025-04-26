package dev.android.simplify.domain.usecase

import dev.android.simplify.domain.repository.AuthRepository

class GetSavedCredentialsUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Pair<String?, String?> {
        return authRepository.getSavedCredentials()
    }
}