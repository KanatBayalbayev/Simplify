package dev.android.simplify.domain.model

import java.io.Serializable

interface AutoReadResolve : Serializable {
    @Suppress("unused")
    fun readResolve(): Any = this
}

sealed class AuthError : Exception(), AutoReadResolve {

    data object InvalidEmail : AuthError()

    data object WeakPassword : AuthError()

    data object UserNotFound : AuthError()

    data object WrongPassword : AuthError()

    data object EmailAlreadyInUse : AuthError()

    data object NetworkError : AuthError()

    data class UnknownError(override val message: String?) : AuthError()
}