package dev.android.simplify.data.mapper

import com.google.firebase.auth.FirebaseUser
import dev.android.simplify.domain.model.User

fun FirebaseUser.toDomainUser(): User {
    return User(
        id = uid,
        email = email.orEmpty(),
        displayName = displayName.orEmpty(),
        photoUrl = photoUrl?.toString().orEmpty()
    )
}