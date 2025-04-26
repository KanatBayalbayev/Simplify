package dev.android.simplify.domain.model

sealed class AuthError : Exception() {
    data object InvalidEmail : AuthError()
    data object WeakPassword : AuthError()
    data object UserNotFound : AuthError()
    data object WrongPassword : AuthError()
    data object EmailAlreadyInUse : AuthError()
    data object NetworkError : AuthError()
    data class UnknownError(override val message: String?) : AuthError()
}