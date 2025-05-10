package dev.android.simplify.app.di

import dev.android.simplify.data.repository.FirebaseAuthRepository
import dev.android.simplify.data.repository.FirebaseChatRepository
import dev.android.simplify.data.repository.FirebaseUserRepository
import dev.android.simplify.data.source.local.credentials.CredentialsStorage
import dev.android.simplify.domain.repository.AuthRepository
import dev.android.simplify.domain.repository.ChatRepository
import dev.android.simplify.domain.repository.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<AuthRepository> { FirebaseAuthRepository(get(), get()) }
    single<UserRepository> { FirebaseUserRepository(get(), get()) }
    single<ChatRepository> { FirebaseChatRepository(get(), get(), get()) }
    single { CredentialsStorage(androidContext()) }
}