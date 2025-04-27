package dev.android.simplify.domain.repository

import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    /**
     * Получение информации о текущем пользователе
     */
    fun getCurrentUserProfile(): Flow<User?>

    /**
     * Получение пользователя по ID
     */
    suspend fun getUserById(userId: String): User?

    /**
     * Поиск пользователей по email
     */
    suspend fun searchUsersByEmail(email: String): List<User>

    /**
     * Обновление профиля пользователя
     */
    suspend fun updateUserProfile(user: User): AuthResult<Unit>

    /**
     * Создание или обновление профиля пользователя
     */
    suspend fun createOrUpdateUserProfile(user: User): AuthResult<Unit>

    /**
     * Проверка существования пользователя по email
     */
    suspend fun isEmailExists(email: String): Boolean
}