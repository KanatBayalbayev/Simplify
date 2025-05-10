package dev.android.simplify.app.di

import dev.android.simplify.data.source.local.LocalChatDataSource
import dev.android.simplify.data.source.local.database.ChatDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {
    // База данных Room
    single { ChatDatabase.getInstance(androidContext()) }

    // DAO
    single { get<ChatDatabase>().chatDao() }
    single { get<ChatDatabase>().userDao() }
    single { get<ChatDatabase>().messageDao() }
    single { get<ChatDatabase>().chatWithUserDao() }

    // Локальный источник данных
    single {
        LocalChatDataSource(
            chatDao = get(),
            userDao = get(),
            messageDao = get(),
            chatWithUserDao = get()
        )
    }
}