package dev.android.simplify.domain.repository

import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun isUserAuthenticated(): Boolean

    fun getCurrentUser(): Flow<AuthResult<User?>>

    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult<User>

    suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult<User>

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit>

    suspend fun signOut(): AuthResult<Unit>
}