package dev.android.simplify.presentation.auth.common

import dev.android.simplify.domain.model.AuthError

fun mapAuthErrorToMessage(error: Throwable?): String {

    return when (error) {
        is AuthError.InvalidEmail -> "Неверный формат email"
        is AuthError.WeakPassword -> "Пароль слишком слабый. Используйте минимум 6 символов"
        is AuthError.UserNotFound -> "Пользователь с таким email не найден"
        is AuthError.WrongPassword -> "Неверный пароль"
        is AuthError.EmailAlreadyInUse -> "Email уже используется другим аккаунтом"
        is AuthError.NetworkError -> "Ошибка сети. Проверьте подключение к интернету"
        else -> "Произошла ошибка: ${error?.message ?: "Неизвестная ошибка"}"
    }
}