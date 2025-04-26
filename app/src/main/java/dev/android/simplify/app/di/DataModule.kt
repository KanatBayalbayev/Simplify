package dev.android.simplify.app.di

import dev.android.simplify.data.repository.FirebaseAuthRepository
import dev.android.simplify.domain.repository.AuthRepository
import org.koin.dsl.module

val dataModule = module {
    single<AuthRepository> { FirebaseAuthRepository(get()) }
}