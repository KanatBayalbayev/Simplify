package dev.android.simplify.app.di

import dev.android.simplify.domain.usecase.ForgotPasswordUseCase
import dev.android.simplify.domain.usecase.GetCurrentUserUseCase
import dev.android.simplify.domain.usecase.IsUserAuthenticatedUseCase
import dev.android.simplify.domain.usecase.SignInUseCase
import dev.android.simplify.domain.usecase.SignOutUseCase
import dev.android.simplify.domain.usecase.SignUpUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { SignInUseCase(get()) }
    factory { SignUpUseCase(get()) }
    factory { ForgotPasswordUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { IsUserAuthenticatedUseCase(get()) }
    factory { SignOutUseCase(get()) }
}