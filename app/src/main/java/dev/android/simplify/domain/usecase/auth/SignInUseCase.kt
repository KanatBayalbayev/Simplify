package dev.android.simplify.domain.usecase.auth

import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.repository.AuthRepository

class SignInUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): AuthResult<User> {
        return authRepository.signInWithEmailAndPassword(email, password)
    }
}