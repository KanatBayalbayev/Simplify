package dev.android.simplify.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.android.simplify.data.mapper.toDomainUser
import dev.android.simplify.data.model.FirebaseUserData
import dev.android.simplify.domain.model.AuthError
import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseUserRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : UserRepository {

    private val usersRef = firebaseDatabase.getReference("users")

    override fun getCurrentUserProfile(): Flow<User?> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid ?: run {
            trySend(null)
            return@callbackFlow
        }

        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firebaseUserData = snapshot.getValue(FirebaseUserData::class.java)
                val user = firebaseUserData?.toDomainUser() ?: User(
                    id = currentUserId,
                    email = firebaseAuth.currentUser?.email ?: "",
                    displayName = firebaseAuth.currentUser?.displayName ?: ""
                )
                trySend(user)
            }

            override fun onCancelled(error: DatabaseError) {
                // Ошибка при получении данных
                // Можно обработать при необходимости
            }
        }

        usersRef.child(currentUserId).addValueEventListener(userListener)

        awaitClose {
            usersRef.child(currentUserId).removeEventListener(userListener)
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            val firebaseUserData = snapshot.getValue(FirebaseUserData::class.java)
            firebaseUserData?.toDomainUser()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun searchUsersByEmail(email: String): List<User> {
        return try {
            val snapshot = usersRef.orderByChild("email").startAt(email).endAt(email + "\uf8ff").get().await()
            val users = mutableListOf<User>()

            for (childSnapshot in snapshot.children) {
                val firebaseUserData = childSnapshot.getValue(FirebaseUserData::class.java)
                firebaseUserData?.let {
                    // Исключаем текущего пользователя из результатов поиска
                    if (it.id != firebaseAuth.currentUser?.uid) {
                        users.add(it.toDomainUser())
                    }
                }
            }

            users
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateUserProfile(user: User): AuthResult<Unit> {
        return try {
            val firebaseUserData = FirebaseUserData(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl,
                lastUpdated = Date()
            )

            usersRef.child(user.id).setValue(firebaseUserData).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(AuthError.UnknownError(e.message))
        }
    }

    override suspend fun createOrUpdateUserProfile(user: User): AuthResult<Unit> {
        return try {
            val currentUserId = user.id.ifEmpty { firebaseAuth.currentUser?.uid ?: return AuthResult.Error(AuthError.UserNotFound) }

            val firebaseUserData = FirebaseUserData(
                id = currentUserId,
                email = user.email,
                displayName = user.displayName,
                photoUrl = user.photoUrl,
                lastUpdated = Date()
            )

            usersRef.child(currentUserId).setValue(firebaseUserData).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(AuthError.UnknownError(e.message))
        }
    }

    override suspend fun isEmailExists(email: String): Boolean {
        return try {
            val snapshot = usersRef.orderByChild("email").equalTo(email).get().await()
            !snapshot.children.none()
        } catch (e: Exception) {
            false
        }
    }
}