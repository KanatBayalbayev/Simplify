package dev.android.simplify.domain.usecase.auth

import dev.android.simplify.domain.repository.AuthRepository

class IsUserAuthenticatedUseCase(private val authRepository: AuthRepository) {

    operator fun invoke(): Boolean {
        return authRepository.isUserAuthenticated()
    }
}