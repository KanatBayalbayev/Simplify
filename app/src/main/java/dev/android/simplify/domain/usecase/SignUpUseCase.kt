package dev.android.simplify.domain.usecase

import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.repository.AuthRepository

class SignUpUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): AuthResult<User> {
        return authRepository.signUpWithEmailAndPassword(email, password)
    }
}