package dev.android.simplify.presentation.chat.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.android.simplify.presentation.chat.components.ChatListItem
import dev.android.simplify.presentation.common.ChatListSkeletonLoader
import dev.android.simplify.presentation.common.EmptyStateView
import dev.android.simplify.presentation.common.LoadingView
import dev.android.simplify.presentation.common.UserSearchBottomSheet
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHomeScreen(
    onNavigateToChat: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ChatHomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val chats by viewModel.chats.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

    // Обработка навигации к чату после его создания
    LaunchedEffect(uiState.createdChatId) {
        uiState.createdChatId?.let { chatId ->
            onNavigateToChat(chatId)
        }
    }

    // Обработка выхода из аккаунта
    LaunchedEffect(uiState.isSignedOut) {
        if (uiState.isSignedOut) {
            onLogout()
        }
    }

    // Обработка ошибок
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Чаты",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Выйти из аккаунта"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openSearchSheet() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Новый чат")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Показываем скелетон во время загрузки
            if (uiState.isLoading) {
                ChatListSkeletonLoader(
                    modifier = Modifier.fillMaxSize()
                )
            } else if (chats.isEmpty()) {
                // Показываем сообщение о пустом списке чатов
                EmptyStateView(
                    message = "У вас пока нет чатов.\nНажмите + чтобы начать новый разговор.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Показываем список чатов
                LazyColumn {
                    items(chats) { chat ->
                        ChatListItem(
                            chatWithUser = chat,
                            onClick = { onNavigateToChat(chat.chat.id) }
                        )
                        Divider()
                    }
                }
            }

            // Отображаем индикатор загрузки, если идет создание чата
            if (uiState.isCreatingChat) {
                LoadingView(message = "Создание чата...")
            }
        }
    }

    // Bottom Sheet для поиска пользователей
    if (uiState.isSearchSheetOpen) {
        UserSearchBottomSheet(
            onDismiss = { viewModel.closeSearchSheet() },
            onUserSelected = { user -> viewModel.createChat(user) },
            searchUsers = { viewModel.searchUsers(it) },
            userSearchResults = uiState.searchResults,
            isLoading = uiState.isSearching,
            sheetState = bottomSheetState
        )
    }
}