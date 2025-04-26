package dev.android.simplify.app.di

import dev.android.simplify.presentation.auth.forgot_password.ForgotPasswordViewModel
import dev.android.simplify.presentation.auth.login.LoginViewModel
import dev.android.simplify.presentation.auth.register.RegisterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { LoginViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get()) }
}