package dev.android.simplify.app.di

import dev.android.simplify.domain.usecase.auth.ClearCredentialsUseCase
import dev.android.simplify.domain.usecase.auth.ForgotPasswordUseCase
import dev.android.simplify.domain.usecase.auth.GetCurrentUserUseCase
import dev.android.simplify.domain.usecase.auth.GetSavedCredentialsUseCase
import dev.android.simplify.domain.usecase.auth.HasSavedCredentialsUseCase
import dev.android.simplify.domain.usecase.auth.IsUserAuthenticatedUseCase
import dev.android.simplify.domain.usecase.auth.SaveCredentialsUseCase
import dev.android.simplify.domain.usecase.auth.SignInUseCase
import dev.android.simplify.domain.usecase.auth.SignOutUseCase
import dev.android.simplify.domain.usecase.auth.SignUpUseCase
import dev.android.simplify.domain.usecase.chat.CreateChatUseCase
import dev.android.simplify.domain.usecase.chat.GetChatMessagesUseCase
import dev.android.simplify.domain.usecase.chat.GetUnreadMessagesCountUseCase
import dev.android.simplify.domain.usecase.chat.GetUserChatsUseCase
import dev.android.simplify.domain.usecase.chat.GetUserStatusUseCase
import dev.android.simplify.domain.usecase.chat.MarkMessagesAsReadUseCase
import dev.android.simplify.domain.usecase.chat.SendMessageUseCase
import dev.android.simplify.domain.usecase.chat.SetUserStatusUseCase
import dev.android.simplify.domain.usecase.user.CreateOrUpdateUserProfileUseCase
import dev.android.simplify.domain.usecase.user.GetCurrentUserProfileUseCase
import dev.android.simplify.domain.usecase.user.GetUserByIdUseCase
import dev.android.simplify.domain.usecase.user.SearchUsersByEmailUseCase
import dev.android.simplify.domain.usecase.user.UpdateUserProfileUseCase
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

    // User use cases
    factory { GetCurrentUserProfileUseCase(get()) }
    factory { GetUserByIdUseCase(get()) }
    factory { SearchUsersByEmailUseCase(get()) }
    factory { UpdateUserProfileUseCase(get()) }
    factory { CreateOrUpdateUserProfileUseCase(get()) }

    // Chat use cases
    factory { GetUserChatsUseCase(get()) }
    factory { CreateChatUseCase(get()) }
    factory { GetChatMessagesUseCase(get()) }
    factory { SendMessageUseCase(get()) }
    factory { MarkMessagesAsReadUseCase(get()) }
    factory { GetUserStatusUseCase(get()) }
    factory { SetUserStatusUseCase(get()) }
    factory { GetUnreadMessagesCountUseCase(get()) }
}