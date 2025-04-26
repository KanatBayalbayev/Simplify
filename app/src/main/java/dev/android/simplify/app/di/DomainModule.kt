package dev.android.simplify.app.di

import dev.android.simplify.domain.usecase.ClearCredentialsUseCase
import dev.android.simplify.domain.usecase.ForgotPasswordUseCase
import dev.android.simplify.domain.usecase.GetCurrentUserUseCase
import dev.android.simplify.domain.usecase.GetSavedCredentialsUseCase
import dev.android.simplify.domain.usecase.HasSavedCredentialsUseCase
import dev.android.simplify.domain.usecase.IsUserAuthenticatedUseCase
import dev.android.simplify.domain.usecase.SaveCredentialsUseCase
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

    factory { SaveCredentialsUseCase(get()) }
    factory { GetSavedCredentialsUseCase(get()) }
    factory { HasSavedCredentialsUseCase(get()) }
    factory { ClearCredentialsUseCase(get()) }
}