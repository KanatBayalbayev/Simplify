package dev.android.simplify.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dev.android.simplify.data.mapper.toDomainUser
import dev.android.simplify.data.source.local.CredentialsStorage
import dev.android.simplify.domain.model.AuthError
import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val credentialsStorage: CredentialsStorage
) : AuthRepository {

    override fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUser(): Flow<AuthResult<User?>> = callbackFlow {
        trySend(AuthResult.Loading)

        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            val result = if (user != null) {
                AuthResult.Success(user.toDomainUser())
            } else {
                AuthResult.Success(null)
            }
            trySend(result)
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user?.toDomainUser()
                ?: return AuthResult.Error(AuthError.UnknownError("User is null after sign in"))
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseAuthException(e))
        }
    }

    override suspend fun signUpWithEmailAndPassword(email: String, password: String): AuthResult<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user?.toDomainUser()
                ?: return AuthResult.Error(AuthError.UnknownError("User is null after sign up"))
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseAuthException(e))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseAuthException(e))
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            firebaseAuth.signOut()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(AuthError.UnknownError(e.message))
        }
    }

    override fun saveCredentials(email: String, password: String) {
        credentialsStorage.saveCredentials(email, password)
    }

    override fun getSavedCredentials(): Pair<String?, String?> {
        val email = credentialsStorage.getSavedEmail()
        val password = credentialsStorage.getSavedPassword()
        return Pair(email, password)
    }

    override fun hasSavedCredentials(): Boolean {
        return credentialsStorage.hasSavedCredentials()
    }

    override fun clearCredentials() {
        credentialsStorage.clearCredentials()
    }

    private fun mapFirebaseAuthException(exception: Exception): AuthError {
        return when (exception) {
            is FirebaseAuthInvalidUserException -> AuthError.UserNotFound
            is FirebaseAuthInvalidCredentialsException -> {
                if (exception.message?.contains("password") == true) {
                    AuthError.WrongPassword
                } else {
                    AuthError.InvalidEmail
                }
            }
            is FirebaseAuthWeakPasswordException -> AuthError.WeakPassword
            is FirebaseAuthUserCollisionException -> AuthError.EmailAlreadyInUse
            is FirebaseNetworkException -> AuthError.NetworkError
            else -> AuthError.UnknownError(exception.message)
        }
    }
}