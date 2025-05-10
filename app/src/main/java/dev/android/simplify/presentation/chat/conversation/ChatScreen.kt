package dev.android.simplify.presentation.chat.conversation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.android.simplify.domain.model.Message
import dev.android.simplify.presentation.common.ChatSkeletonLoader
import dev.android.simplify.presentation.common.LoadingView
import dev.android.simplify.presentation.common.UserAvatar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = koinViewModel(parameters = { parametersOf(chatId) })
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.markMessagesAsRead()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Log.e("ChatScreen", "Error shown in snackbar: $error")
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
                    // Use the chat title from UI state which now handles self-chat scenarios
                    Text(text = uiState.chatTitle)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    // Show avatar (either other user's or current user's in self-chat)
                    uiState.otherUser?.let { user ->
                        UserAvatar(
                            photoUrl = user.photoUrl,
                            displayName = user.displayName,
                            size = 36.dp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            MessageInputBar(
                messageText = uiState.messageText,
                onMessageTextChanged = viewModel::onMessageTextChanged,
                onSendClick = { viewModel.sendMessage(uiState.messageText) },
                isSending = uiState.isSending
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Check if messages are loading
            if (uiState.isLoading) {
                // Show skeleton loader
                ChatSkeletonLoader(
                    modifier = Modifier.fillMaxSize()
                )
            } else if (messages.isEmpty()) {
                // Show empty chat message with appropriate text based on chat type
                Text(
                    text = if (uiState.isSelfChat)
                        "Это ваши заметки. Начните писать!"
                    else
                        "Нет сообщений. Начните общение!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                // Show message list
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages) { message ->
                        // In self-chat, still style messages as if they're from current user
                        // This keeps the UI consistent with the user's expectations
                        val isFromCurrentUser = message.senderId == currentUser?.id
                        MessageItem(
                            message = message,
                            isFromCurrentUser = isFromCurrentUser,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Show loading indicator when sending a message
            if (uiState.isSending) {
                LoadingView(message = "Отправка сообщения...")
            }
        }
    }
}


@Composable
fun MessageItem(
    message: Message,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                        bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isFromCurrentUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isFromCurrentUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Строка со временем и статусом доставки/прочтения
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            // Отображаем время отправки сообщения
            Text(
                text = formatMessageTime(message.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            // Показываем индикаторы статуса только для сообщений текущего пользователя
            if (isFromCurrentUser) {
                Spacer(modifier = Modifier.width(4.dp))

                // Показываем индикатор статуса (одна или две галочки)
                if (message.isRead) {
                    // Две галочки - сообщение прочитано
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Прочитано",
                        tint = Color(0xFF4CAF50), // Зеленый цвет
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    // Одна галочка - сообщение доставлено, но не прочитано
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Доставлено",
                        tint = Color(0xFF9E9E9E), // Серый цвет
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageTextChanged,
            placeholder = { Text("Сообщение") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSendClick,
            enabled = messageText.isNotBlank() && !isSending,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Отправить",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

private fun formatMessageTime(date: Date): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}